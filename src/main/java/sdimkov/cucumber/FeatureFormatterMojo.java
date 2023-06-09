package sdimkov.cucumber;


import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class FeatureFormatterMojo extends AbstractMojo {

    private final String[] featureExtensions = new String[]{"feature"};

    @Parameter(property = "applyFormatting", defaultValue = "true")
    private boolean applyFormatting;

    @Parameter(property = "applyRestrictions", defaultValue = "true")
    private boolean applyRestrictions;

    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File baseDir;

    // Indents

    @Parameter(property = "format.featureIndent", defaultValue = "0")
    private int featureIndent;

    @Parameter(property = "format.backgroundIndent", defaultValue = "2")
    private int backgroundIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "2")
    private int scenarioIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "4")
    private int givenIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "5")
    private int whenIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "5")
    private int thenIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "6")
    private int andIndent;

    // Blank lines before

    @Parameter(property = "format.featureBlankLines", defaultValue = "0")
    private int featureBlankLines;

    @Parameter(property = "format.backgroundBlankLines", defaultValue = "1")
    private int backgroundBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "2")
    private int scenarioBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "0")
    private int givenBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "0")
    private int whenBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "0")
    private int thenBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "0")
    private int andBlankLines;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Iterator<File> iterator = FileUtils.iterateFiles(baseDir, featureExtensions, true);
        boolean hasRestrictionIssues = false;
        Set<String> featureFileNames = new HashSet<>();
        Set<String> featureNames = new HashSet<>();
        Set<String> scenarioNames = new HashSet<>();
        List<String> restrictorIssues = new ArrayList<>();

        while (iterator.hasNext()) {
            File featureFile = iterator.next();
            try {
                getLog().debug("Processing " + featureFile.getAbsolutePath());

                if (applyFormatting) doFormat(featureFile);

                if (applyRestrictions) {
                    doRestrict(featureFile, featureFileNames, featureNames, scenarioNames);
                }

            } catch (IOException e) {
                getLog().error("Unable to process " + featureFile.getAbsolutePath(), e);
            } catch (IllegalStateException t) {
                String restrictorError = t.getMessage();
                getLog().error("Restrictor found an issue: " + restrictorError);
                restrictorIssues.add(restrictorError);
                hasRestrictionIssues = true;
            } catch (Throwable t) {
                getLog().error("Unhandled exception:", t);
            }
        }

        if (hasRestrictionIssues) {
            throw new MojoFailureException("Feature files contain issues, fix them to proceed! List of issues:\n" + restrictorIssues);
        }
    }

    private void doRestrict(File featureFile, Set<String> featureFileNames,
                            Set<String> featureNames, Set<String> scenarioNames)
            throws IOException {
        new FluentRestrictor(featureFile)
        .tryToUpdateFileNameSet(featureFileNames)
        .tryToUpdateFeatureNameSet(featureNames)
        .setNameRestrictionFor(Arrays.asList("Scenario:", "Scenario Outline:"), scenarioNames);
    }

    private void doFormat(File featureFile) throws IOException {
        new FluentFormatter(featureFile)
                .setBlankLinesBefore("Feature:", featureBlankLines)
                .setBlankLinesBefore("Background:", backgroundBlankLines)
                .setBlankLinesBefore("Rule:", backgroundBlankLines)
                .setBlankLinesBefore("Scenario:", scenarioBlankLines)
                .setBlankLinesBefore("Scenario Outline:", scenarioBlankLines)
                .setBlankLinesBefore("Given", givenBlankLines)
                .setBlankLinesBefore("When", whenBlankLines)
                .setBlankLinesBefore("Then", thenBlankLines)
                .setBlankLinesBefore("And", andBlankLines)
                .setBlankLinesBefore("*", andBlankLines)

                .setIndent("Feature:", featureIndent)
                .setIndent("Background:", backgroundIndent)
                .setIndent("Rule:", backgroundIndent)
                .setIndent("Scenario:", scenarioIndent)
                .setIndent("Scenario Outline:", scenarioIndent)
                .setIndent("Given", givenIndent)
                .setIndent("When", whenIndent)
                .setIndent("Then", thenIndent)
                .setIndent("And", andIndent)
                .setIndent("*", andIndent)

                .format().save();
    }

}
