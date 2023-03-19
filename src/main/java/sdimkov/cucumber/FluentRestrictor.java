package sdimkov.cucumber;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class FluentRestrictor {

    File file;
    Path inputPath;
    String pathToCurrentFile;
    List<String> input;

    /**
     * @param file the target file for formatting
     * @throws IOException
     */
    public FluentRestrictor(File file) throws IOException {
        this.file = file;
        this.inputPath = file.toPath();
        this.pathToCurrentFile = "\nPath to current file:\n " + this.inputPath;
        this.input = Files.readAllLines(inputPath, Charset.defaultCharset());
    }

    public FluentRestrictor tryToUpdateFileNameSet(Set<String> featureFileNames) {
        String fileName = file.getName();
        if (featureFileNames.contains(fileName))
            throw new IllegalStateException("Found more than one feature file with file name:\n "
                    + fileName + pathToCurrentFile);
        featureFileNames.add(fileName);
        return this;
    }

    public FluentRestrictor tryToUpdateFeatureNameSet(Set<String> featureNames) {
        String featureName;

        for (String line : input) {
            String word = getFirstWord(line);
            if ("Feature:".equals(word)) {
                featureName = getAllButFirstWord(line, word);
                if (featureNames.contains(featureName))
                    throw new IllegalStateException("Found more than one feature file with the same feature title text:\n "
                            + featureName + pathToCurrentFile);
                featureNames.add(featureName);
            }
        }
        return this;
    }

    public FluentRestrictor setNameRestrictionFor(List<String> entity, Set<String> scenarioNames) {
        for (String line : input) {
            String word = getFirstWord(line);

            if (entity.contains(word)) {
                String entityValue = getAllButFirstWord(line, word);
                //System.out.println("getallbutfirstword: " + entityValue);
                if (word.contains("Scenario")) {
                    if (scenarioNames.contains(entityValue)) {
                        throw new IllegalStateException("Found more than one scenario with same name:\n "
                                + entityValue + pathToCurrentFile);
                    }
                    scenarioNames.add(entityValue);
                    //System.out.println("scenarionames: " + scenarioNames);
                }
            }

        }
        return this;
    }

    private String getFirstWord(String line) {
        return line.trim().split(" ")[0];
    }

    private String getAllButFirstWord(String line, String word) {
        return line.replaceFirst(word + "", "").trim();
    }
}
