package steps;

import io.cucumber.java.en.When;
import io.cucumber.java.hr.Kada;


public class AnotherStepDef {

    @When("English when step that has the same method name in another file but different text")
    public void englishWhenStepThatHasTheSameMethodNameInAnotherFile() {
        hrvatskiWhenStepThatIsReused();
    }

    @Kada("Hrvatski when step that is reused")
    public void hrvatskiWhenStepThatIsReused() {
    }

}
