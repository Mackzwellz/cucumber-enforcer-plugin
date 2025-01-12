# cucumber-enforcer-plugin

Maven Plugin to enforce some of the opinionated rules for Cucumber tests.

## Features

All features are toggleable on/off.

- Formatting (works mostly the same as [the original](https://github.com/sdimkov/cucumber-feature-format-plugin), but supports all Cucumber keywords)
- Restricting
    - Fail if duplicates are found (file names or Cucumber feature/background/rule/scenario names); see #2 for reference

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
                            <goal>restrict</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <baseDir>${project.build.testOutputDirectory}</baseDir>
                    <applyRestrictions>true</applyRestrictions>
                </configuration>
            </plugin>
```