package io.github.mackzwellz.cucumber.formatter;


import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;


@Mojo(name = "format", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class FeatureFormatterMojo extends AbstractMojo {

    private final String[] featureExtensions = new String[]{"feature"};

    @Parameter(property = "applyFormatting", defaultValue = "true")
    private boolean applyFormatting;

    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    private File baseDir;

    // Indents

    @Parameter(property = "format.featureIndent", defaultValue = "0")
    private int featureIndent;

    @Parameter(property = "format.backgroundIndent", defaultValue = "2")
    private int backgroundIndent;

    @Parameter(property = "format.scenarioIndent", defaultValue = "2")
    private int scenarioIndent;

    @Parameter(property = "format.givenIndent", defaultValue = "4")
    private int givenIndent;

    @Parameter(property = "format.whenIndent", defaultValue = "5")
    private int whenIndent;

    @Parameter(property = "format.thenIndent", defaultValue = "5")
    private int thenIndent;

    @Parameter(property = "format.andIndent", defaultValue = "6")
    private int andIndent;

    @Parameter(property = "format.starIndent", defaultValue = "6")
    private int starIndent;

    // Blank lines before

    @Parameter(property = "format.featureBlankLines", defaultValue = "0")
    private int featureBlankLines;

    @Parameter(property = "format.backgroundBlankLines", defaultValue = "1")
    private int backgroundBlankLines;

    @Parameter(property = "format.ruleBlankLines", defaultValue = "1")
    private int ruleBlankLines;

    @Parameter(property = "format.scenarioBlankLines", defaultValue = "2")
    private int scenarioBlankLines;

    @Parameter(property = "format.givenBlankLines", defaultValue = "0")
    private int givenBlankLines;

    @Parameter(property = "format.whenBlankLines", defaultValue = "0")
    private int whenBlankLines;

    @Parameter(property = "format.thenBlankLines", defaultValue = "0")
    private int thenBlankLines;

    @Parameter(property = "format.andBlankLines", defaultValue = "0")
    private int andBlankLines;

    @Parameter(property = "format.starBlankLines", defaultValue = "0")
    private int starBlankLines;

    @Override
    public void execute() {
        Iterator<File> iterator = FileUtils.iterateFiles(baseDir, featureExtensions, true);
        while (iterator.hasNext()) {
            File featureFile = iterator.next();
            try {
                getLog().debug("Processing " + featureFile.getAbsolutePath());
                if (applyFormatting) doFormat(featureFile);
            } catch (IOException e) {
                getLog().error("Unable to process " + featureFile.getAbsolutePath(), e);
            } catch (Throwable t) {
                getLog().error("Unhandled exception:", t);
            }
        }
    }

    private void doFormat(File featureFile) throws IOException {
        new FluentFormatter(featureFile)
                .setBlankLinesBefore("Feature:", featureBlankLines)
                .setBlankLinesBefore("Background:", backgroundBlankLines)
                .setBlankLinesBefore("Rule:", ruleBlankLines)
                .setBlankLinesBefore("Scenario:", scenarioBlankLines)
                .setBlankLinesBefore("Scenario Outline:", scenarioBlankLines)
                .setBlankLinesBefore("Given", givenBlankLines)
                .setBlankLinesBefore("When", whenBlankLines)
                .setBlankLinesBefore("Then", thenBlankLines)
                .setBlankLinesBefore("And", andBlankLines)
                .setBlankLinesBefore("*", starBlankLines)

                .setIndent("Feature:", featureIndent)
                .setIndent("Background:", backgroundIndent)
                .setIndent("Rule:", backgroundIndent)
                .setIndent("Scenario:", scenarioIndent)
                .setIndent("Scenario Outline:", scenarioIndent)
                .setIndent("Given", givenIndent)
                .setIndent("When", whenIndent)
                .setIndent("Then", thenIndent)
                .setIndent("And", andIndent)
                .setIndent("*", starIndent)

                .format().save();
    }

}
