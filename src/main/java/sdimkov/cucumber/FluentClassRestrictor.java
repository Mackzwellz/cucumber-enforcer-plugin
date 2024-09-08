package sdimkov.cucumber;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FluentClassRestrictor {

    Set<Class<? extends Annotation>> cucumberAnnotations;
    File stepDefDirectory;
    List<File> stepDefClassFiles;
    List<Class<?>> classes;

    /**
     * @param stepDefDirectory the target directory for restricting
     * @throws IOException
     */
    public FluentClassRestrictor(File stepDefDirectory,
                                 Set<Class<? extends Annotation>> cucumberAnnotations) {
        //load all classes in package and sub-packages, iterate over each
        Iterator<File> files = FileUtils.iterateFiles(stepDefDirectory, new String[]{"java"}, true);
        List<File> stepDefClassFiles = new ArrayList<>();
        files.forEachRemaining(stepDefClassFiles::add);

        cucumberAnnotations.addAll(getDefaultCucumberAnnotations());
        this.cucumberAnnotations = cucumberAnnotations;
        this.stepDefDirectory = stepDefDirectory;
        this.stepDefClassFiles = stepDefClassFiles;

        String standard = Pattern.quote("src" + File.separator + "test" + File.separator + "java");
        String[] projectBaseDirAndPackage = stepDefDirectory.getAbsolutePath().split(standard);

        File compiledStepDefDirectory = new File(projectBaseDirAndPackage[0] + "target" + File.separator + "test-classes");
        // TODO use as 2nd arg: projectBaseDirAndPackage[1].replaceAll("[\\\\/]+", ".")
        this.classes = findFileClasses(compiledStepDefDirectory, "");
        // getClassesFromFiles(stepDefClassFiles);
    }

    private Set<Class<? extends Annotation>> getDefaultCucumberAnnotations() {
        Set<Class<? extends Annotation>> cucumberAnnotations = new HashSet<>();
        //cucumberAnnotations.add(Name.class);
//        try {
//            cucumberAnnotations.add((Class<? extends Annotation>) Class.forName("io.cucumber.java.en.When"));
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        //FileUtils.iterateFiles(stepDefDirectory, new String[]{"class"}, true);

        String mavenContainerDir = Arrays.stream(System.getProperty("java.class.path").split(";")).filter(path -> path.contains(".m2")).findFirst().orElseThrow().split(".m2")[0];
        Path dir = Path.of(mavenContainerDir + ".m2" + File.separator + "repository" + File.separator + "io" + File.separator + "cucumber" + File.separator + "cucumber-java");
        File cucumberJar = findLatestModified(dir, "jar");
        Set<Class<? extends Annotation>> jarAnnotations = null;
        try {
            jarAnnotations = getAnnotationClassesFromJarFile(cucumberJar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cucumberAnnotations.addAll(jarAnnotations);
        return cucumberAnnotations;
    }


    public static Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.out.println("Failed when trying to load class by name " + className + " due to: " + e);
        }
        return null;
    }

    private static File findLatestModified(Path startPath, String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        Iterator<File> files = FileUtils.iterateFiles(
                startPath.toFile(),
                WildcardFileFilter.builder().setWildcards("*" + extension).get(),
                TrueFileFilter.INSTANCE);
        List<File> allFoundFiles = new ArrayList<>();
        files.forEachRemaining(allFoundFiles::add);
        allFoundFiles.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        return allFoundFiles.get(0);
    }

    private static Set<String> getClassNamesFromJarFile(File givenFile) throws IOException {
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(givenFile)) {
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    classNames.add(className);
                }
            }
            return classNames;
        }
    }

    private static Set<Class<? extends Annotation>> getAnnotationClassesFromJarFile(File jarFile) throws IOException {
        Set<String> classNames = getClassNamesFromJarFile(jarFile);
        Set<Class<?>> classes = new HashSet<>(classNames.size());
        try (URLClassLoader cl = URLClassLoader.newInstance(
                new URL[]{new URL("jar:file:" + jarFile + "!/")})) {
            for (String name : classNames) {
                Class<?> clazz = null;
                try {
                    clazz = cl.loadClass(name); // Load the class by its name
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    System.out.println("Failed when trying to load class by name " + name + " due to: " + e);
                }
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }
        return classes.stream().filter(Class::isAnnotation).map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }

    private static Set<Class<? extends Annotation>> getClassesFromFolder(File jarFile) throws IOException {
        Set<String> classNames = getClassNamesFromJarFile(jarFile);
        Set<Class<?>> classes = new HashSet<>(classNames.size());
        try (URLClassLoader cl = URLClassLoader.newInstance(
                new URL[]{new URL("jar:file:" + jarFile + "!/")})) {
            for (String name : classNames) {
                Class<?> clazz = null;
                try {
                    clazz = cl.loadClass(name); // Load the class by its name
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    System.out.println("Failed when trying to load class by name " + name + " due to: " + e);
                }
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }
        return classes.stream().filter(Class::isAnnotation).map(c -> (Class<? extends Annotation>) c).collect(Collectors.toSet());
    }

    public FluentClassRestrictor restrictDuplicateStepMethodNamesAndUsages() {
        Set<String> cucumberAnnotatedMethods = findAllMethodsWithAnnotations(classes, cucumberAnnotations);
        restrict(stepDefClassFiles, cucumberAnnotatedMethods);
        return this;
    }

    private static List<Class<?>> findFileClasses(File directory, String packageName) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            System.err.println("Directory " + directory.getAbsolutePath() + " does not exist.");
            return classes;
        }
        final File[] files = directory.listFiles();
        {
            System.out.println("Directory "
                    + directory.getAbsolutePath()
                    + " has "
                    + files.length
                    + " elements.");
        }
        for (final File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findFileClasses(file, packageName + "." +
                        file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                        + file.getName().substring(0, file.getName().length() - 6);
                if (className.startsWith(".")) {
                    className = className.replaceFirst("\\.", "");
                }
                try {
                    classes.add(Class.forName(className));
                } catch (final ClassNotFoundException cnf) {
                    System.err.println("Cannot load class " + className);
                }
            }
        }
        return classes;
    }


    private static List<Class<?>> getClassesFromFiles(List<File> fileList) {
        List<String> classNames = fileList.stream()
                .map(file -> file.getPath()
                        .replaceAll("[\\\\/]+", ".")
                        .replaceAll("\\.java$", "")
                        .split("src.(main|test).java.")[1])
                //.filter(name -> !name.contains("Mojo"))
                .collect(Collectors.toList());
        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            System.out.println("Trying to load: " + className);
            Class<?> clazz;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            classes.add(clazz);
        }
//        Class<?> clazz = ClassLoader.getSystemClassLoader()
//                .loadClass("sdimkov.cucumber.FluentFormatter");
        return classes;
    }

    //search for usages
    private static void restrict(List<File> fileList, Set<String> cucumberAnnotatedMethods) {
        Map<File, List<String>> fileToContentsMap = fileList.stream().collect(Collectors.toMap(
                file -> file,
                readAllLines()));

        Map<String, List<String>> methodToContentsMap = new HashMap<>();
        for (Map.Entry<File, List<String>> fileListEntry : fileToContentsMap.entrySet()) {
            System.out.println("Checking file: " + fileListEntry.getKey());
            System.out.println("Looking for: " + cucumberAnnotatedMethods);

            for (String methodName : cucumberAnnotatedMethods) {
                List<String> newMatchingLines = processFileForMethod(fileListEntry, methodName);
                if (!newMatchingLines.isEmpty()) {
                    List<String> oldMatchingLines = methodToContentsMap.getOrDefault(methodName, new ArrayList<>());
                    oldMatchingLines.addAll(newMatchingLines);
                    methodToContentsMap.put(methodName, oldMatchingLines);
                }
            }
        }

        StringBuilder errors = new StringBuilder();
        for (Map.Entry<String, List<String>> fileListEntry : methodToContentsMap.entrySet()) {
            errors.append("\nMethod name: ").append(fileListEntry.getKey());
            for (String line : fileListEntry.getValue()) {
                errors.append("\n").append(line);
            }
        }

        if (!methodToContentsMap.isEmpty()) {
            throw new IllegalStateException("Found Cucumber steps definitions reused as regular methods:\n" + errors);
        }
    }

    private static Set<String> findAllMethodsWithAnnotations(List<Class<?>> classes, Set<Class<? extends Annotation>> cucumberAnnotations) {
        //collect all annotated methods
        List<Method> allMethodsInAllClasses = classes.stream()
                .flatMap(aClass -> Arrays.stream(aClass.getMethods()))
                .collect(Collectors.toList());

        Set<String> cucumberAnnotatedMethods = new HashSet<>();
        for (Method method : allMethodsInAllClasses) {
            Set<Class<? extends Annotation>> annotationsForMethod = Arrays.stream(method.getAnnotations()).filter(Objects::nonNull).map(Annotation::annotationType).collect(Collectors.toSet());
            Set<Class<? extends Annotation>> matchingAnnotations = annotationsForMethod.stream().filter(cucumberAnnotations::contains).collect(Collectors.toSet());
            if (!matchingAnnotations.isEmpty()) {
                String methodName = method.getName();
                if (cucumberAnnotatedMethods.contains(methodName)) {
//                    throw new IllegalStateException(
//                            "Found more than one annotated method with the same name. " +
//                                    "Last detected duplicate:\n " + method.getName());
                }
                cucumberAnnotatedMethods.add(methodName);
            }
        }
        return cucumberAnnotatedMethods;
    }

    private static List<String> processFileForMethod(Map.Entry<File, List<String>> fileListEntry, String methodName) {
        List<String> newMatchingLines = new ArrayList<>();
        List<String> value = fileListEntry.getValue();
        for (int i = 0; i < value.size(); i++) {
            String fileLine = value.get(i).trim();
            if (fileLine.contains(methodName) && !fileLine.startsWith("public")) {
                String processedFileLine = fileListEntry.getKey().getPath() + ":" + (i+1) + "\t###\t" + fileLine;
                newMatchingLines.add(processedFileLine);
            }
        }
        return newMatchingLines;
    }

    private static Function<File, List<String>> readAllLines() {
        return file -> {
            try {
                return Files.readAllLines(file.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException("Unable to process " + file.getAbsolutePath(), e);
            }
        };
    }

}
