package io.github.mackzwellz.cucumber.enforcer.restrictors;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class FeatureDuplicateRestrictor {

    File file;
    Path inputPath;
    String pathToCurrentFile;
    List<String> input;

    /**
     * @param file the target file for formatting
     * @throws IOException
     */
    public FeatureDuplicateRestrictor(File file) throws IOException {
        this.file = file;
        this.inputPath = file.toPath();
        this.pathToCurrentFile = "\nPath to current file:\n " + this.inputPath;
        this.input = Files.readAllLines(inputPath, Charset.defaultCharset());
        System.out.println("Processing file:" + file.getName());
    }

    public FeatureDuplicateRestrictor setFileNameRestrictionFor(Set<String> featureFileNames) {
        String fileName = file.getName();
        if (featureFileNames.contains(fileName))
            throw new IllegalStateException("Found more than one feature file with file name:\n "
                    + fileName + pathToCurrentFile);
        featureFileNames.add(fileName);
        return this;
    }

    public FeatureDuplicateRestrictor setNameRestrictionFor(List<String> entities,
                                                            Set<String> featureNames,
                                                            Set<String> backgroundNames,
                                                            Set<String> ruleNames,
                                                            Set<String> scenarioNames) {
        for (String line : input) {
            if (doesLineStartMatch(line, entities)) {
                String word = getFirstWord(line);
                String entityValue = getAllButFirstWord(line, word).replaceAll(" (Outline|Template):", "").trim();
                //System.out.println("getallbutfirstword: " + entityValue);
                if (word.startsWith("Feature")) {
                    if (featureNames.contains(entityValue))
                        throw new IllegalStateException("Found more than one feature file with the same feature title text:\n "
                                + entityValue + pathToCurrentFile);
                    featureNames.add(entityValue);
                }
                if (word.startsWith("Background")) {
                    if (featureNames.contains(entityValue))
                        throw new IllegalStateException("Found more than one background with the same text:\n "
                                + entityValue + pathToCurrentFile);
                    backgroundNames.add(entityValue);
                }
                if (word.startsWith("Rule")) {
                    if (ruleNames.contains(entityValue))
                        throw new IllegalStateException("Found more than one rule with the same text:\n "
                                + entityValue + pathToCurrentFile);
                    ruleNames.add(entityValue);
                }
                if (word.startsWith("Scenario")) {
                    if (scenarioNames.contains(entityValue)) {
                        throw new IllegalStateException("Found more than one scenario with same name:\n "
                                + entityValue + pathToCurrentFile);
                    }
                    scenarioNames.add(entityValue);
                }


            }
        }
        return this;
    }

    private String getFirstWord(String line) {
        return line.trim().split(" ")[0];
    }

    private boolean doesLineStartMatch(String line, List<String> words) {
        return words.stream().anyMatch(word -> line.trim().startsWith(word));
    }

    private String getAllButFirstWord(String line, String word) {
        return line.replaceFirst(word, "").trim();
    }

}
