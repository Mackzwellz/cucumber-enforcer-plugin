# cucumber-enforcer-plugin

Maven Plugin to enforce some of the opinionated rules for Cucumber tests.

## Features

All features are toggleable on/off.

- Formatting (works mostly the same as [the original](https://github.com/sdimkov/cucumber-feature-format-plugin), but supports all Cucumber keywords)
- Restricting/Enforcing
    - Fail if duplicates are found (file names or Cucumber feature/background/rule/scenario names)
      - see #2 for reference
      - supports any programming language
      - supports only english Cucumber keywords
    - Fail if Cucumber steps are reused in step definition code
      - see #4 for reference 
      - supports Java only
      - supports all Cucumber annotations/keywords for step definitions

### Formatting

Put below snippet in your `pom.xml` and run `mvn clean package`:

```
        <plugin>
                <groupId>sdimkov</groupId>
                <artifactId>cucumber-feature-format-plugin</artifactId>
                <version>1.0.1-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>format-features</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>format</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <baseDir>${project.build.testDirectory}</baseDir>
                    <applyFormatting>true</applyFormatting>
                </configuration>
            </plugin>
```

### Restricting

Put below snippet in your `pom.xml` and run `mvn clean package`:

```
        <plugin>
                <groupId>sdimkov</groupId>
                <artifactId>cucumber-feature-format-plugin</artifactId>
                <version>1.0.1-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>restrict-features</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>enforce</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <baseDir>${project.build.testOutputDirectory}</baseDir>
                    <restrictDuplicateFeatures>true</restrictDuplicateFeatures>
                    <restrictReusedSteps>true</restrictReusedSteps>
                </configuration>
            </plugin>
```