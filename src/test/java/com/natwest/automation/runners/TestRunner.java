// src/test/java/com/natwest/automation/runners/TestRunner.java
package com.natwest.automation.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * JUnit Test Runner class to execute Cucumber features using Serenity.
 * This class integrates Cucumber tests with JUnit and Serenity reporting.
 */
@RunWith(CucumberWithSerenity.class) // Use Serenity's runner for integration
@CucumberOptions(
        features = "src/test/resources/features", // Location of the feature files
        glue = "com.natwest.automation.steps",    // Package containing step definition classes
//        tags = System.getProperty("tags"),                     // Execute scenarios that are not tagged with @Ignore
        plugin = {"pretty",                       // Pretty print console output
                  "html:target/cucumber-reports/cucumber-html-report.html", // Basic HTML report
                  "json:target/cucumber-reports/cucumber.json",           // JSON report for other tools
                  "junit:target/cucumber-reports/cucumber.xml"            // JUnit XML report
                 }
)
public class TestRunner {
    // This class remains empty
    // Configuration is handled by annotations
}

