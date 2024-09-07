package sdimkov.cucumber;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
        this.classes = getClassesFromFiles(stepDefClassFiles);
    }

    private Set<Class<? extends Annotation>> getDefaultCucumberAnnotations() {
        Set<Class<? extends Annotation>> cucumberAnnotations = new HashSet<>();
        cucumberAnnotations.add(Deprecated.class);
//        try {
//            cucumberAnnotations.add((Class<? extends Annotation>) Class.forName("cucumber.When"));
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        return cucumberAnnotations;
    }

    public FluentClassRestrictor restrictDuplicateStepMethodNamesAndUsages() {
        Set<String> cucumberAnnotatedMethods = findAllMethodsWithAnnotations(classes, cucumberAnnotations);
        restrict(stepDefClassFiles, cucumberAnnotatedMethods);
        return this;
    }

    private static List<Class<?>> getClassesFromFiles(List<File> fileList) {
        List<String> classNames = fileList.stream()
                .map(file -> file.getPath()
                        .replaceAll("[\\\\/]+", ".")
                        .replaceAll("\\.java$", "")
                        .split("src.main.java.")[1])
                .filter(name -> !name.contains("Mojo"))
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
                if (newMatchingLines.size() > 1) {
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
                errors.append(line);
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
                if (cucumberAnnotatedMethods.contains(methodName))
                    throw new IllegalStateException(
                            "Found more than one annotated method with the same name. " +
                                    "Last detected duplicate:\n " + method.getName());
                cucumberAnnotatedMethods.add(methodName);
            }
        }
        return cucumberAnnotatedMethods;
    }

    private static List<String> processFileForMethod(Map.Entry<File, List<String>> fileListEntry, String methodName) {
        List<String> newMatchingLines = new ArrayList<>();
        List<String> value = fileListEntry.getValue();
        for (int i = 0; i < value.size(); i++) {
            String fileLine = value.get(i);
            if (fileLine.contains(methodName)) {
                String processedFileLine = fileListEntry.getKey().getPath() + ":" + i + "\t###\t" + fileLine;
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
