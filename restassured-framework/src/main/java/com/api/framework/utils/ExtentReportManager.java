package com.api.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * =========================================================
 * ExtentReportManager.java
 * =========================================================
 * Manages the lifecycle of Extent Reports:
 *   - Creates the single ExtentReports instance (singleton)
 *   - Creates/manages per-test ExtentTest instances
 *   - Flushes report to disk after all tests complete
 *
 * Thread-safe: Uses ThreadLocal<ExtentTest> to support
 * parallel test execution safely.
 *
 * Usage in tests (handled by BaseTest):
 *   ExtentReportManager.createTest("My Test");
 *   ExtentReportManager.getTest().pass("Step passed");
 *   ExtentReportManager.getTest().fail("Step failed");
 * =========================================================
 */
public class ExtentReportManager {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);

    // Singleton ExtentReports instance
    private static ExtentReports extentReports;

    // ThreadLocal ensures each parallel thread has its own ExtentTest
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // Private constructor - static utility class
    private ExtentReportManager() {}

    /**
     * Initializes ExtentReports with Spark HTML reporter.
     * Called once before the test suite (in @BeforeSuite).
     */
    public static synchronized ExtentReports initReports() {
        if (extentReports == null) {
            // Generate timestamped report filename
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String reportPath = "reports/ExtentReport_" + timestamp + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            // Configure the reporter
            sparkReporter.config().setDocumentTitle("API Test Automation Report");
            sparkReporter.config().setReportName("REST Assured Framework - Test Results");
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setEncoding("UTF-8");
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // System info shown in report overview
            extentReports.setSystemInfo("Framework",     "REST Assured + TestNG");
            extentReports.setSystemInfo("Language",      "Java 11");
            extentReports.setSystemInfo("Build Tool",    "Maven");
            extentReports.setSystemInfo("Environment",   System.getProperty("env", "QA").toUpperCase());
            extentReports.setSystemInfo("Base URL",      ConfigReader.getBaseUrl());
            extentReports.setSystemInfo("Executed By",   System.getProperty("user.name", "CI"));
            extentReports.setSystemInfo("OS",            System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version",  System.getProperty("java.version"));

            log.info("Extent Report initialized at: {}", reportPath);
        }
        return extentReports;
    }

    /**
     * Creates a new ExtentTest node for a test method.
     * Called in @BeforeMethod.
     *
     * @param testName  Test method name shown in report
     */
    public static ExtentTest createTest(String testName) {
        return createTest(testName, "");
    }

    /**
     * Creates a new ExtentTest node with description.
     */
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = extentReports.createTest(testName, description);
        extentTest.set(test);
        log.debug("ExtentTest created for: [{}]", testName);
        return test;
    }

    /**
     * Returns the ExtentTest for the current thread.
     * Use this in test methods to log steps.
     */
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    /**
     * Removes the current thread's ExtentTest.
     * Call in @AfterMethod to clean up.
     */
    public static void removeTest() {
        extentTest.remove();
    }

    /**
     * Flushes all test results to the HTML report file.
     * MUST be called in @AfterSuite, otherwise the report file is incomplete.
     */
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            log.info("Extent Report flushed successfully.");
        }
    }
}
