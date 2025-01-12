package tests;

import sdimkov.cucumber.restrictors.StepReuseRestrictor;

import java.io.File;
import java.util.HashSet;

//TODO rework into proper test
public class FluentClassRestrictorRunner {

    public static void main(String[] args) {
        String defaultPath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator + "sdimkov" + File.separator + "cucumber" + File.separator + "steps";
        StepReuseRestrictor a = new StepReuseRestrictor(new File(defaultPath), new HashSet<>());
        a.restrictDuplicateStepMethodNamesAndUsages();
    }
}
