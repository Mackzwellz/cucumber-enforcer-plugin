package sdimkov.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ExampleStepDef {

    public void publicNonAnnotatedMethodThatIsNotRestricted() {
        publicNonAnnotatedMethodThatIsNotRestricted();
        publicNonCucumberAnnotatedMethodThatIsAlsoNotRestricted();
    }

    @Deprecated
    public void publicNonCucumberAnnotatedMethodThatIsAlsoNotRestricted() {
    }

    @When("English when step that has the same method name in another file")
    public void englishWhenStepThatHasTheSameMethodNameInAnotherFile() {
        englishAndStepThatIsReused();
    }


    @Given("English given step that is detected but not restricted")
    public void englishGivenStepThatIsDetectedButNotRestricted() {
        englishAndStepThatIsReused();
    }


    @And("English and step that is reused")
    public void englishAndStepThatIsReused() {
        englishAndStepThatIsReused();
    }
}
