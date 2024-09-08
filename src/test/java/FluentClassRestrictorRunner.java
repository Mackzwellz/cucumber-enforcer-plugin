import sdimkov.cucumber.FluentClassRestrictor;

import java.io.File;
import java.util.HashSet;

public class FluentClassRestrictorRunner {

    public static void main(String[] args) {
        String defaultPath = new File("").getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator + "sdimkov" + File.separator + "cucumber" + File.separator + "steps";
        FluentClassRestrictor a = new FluentClassRestrictor(new File(defaultPath), new HashSet<>());
        a.restrictDuplicateStepMethodNamesAndUsages();
    }
}
