package com.api.framework.tests;

import com.api.framework.base.BaseApi;
import com.api.framework.services.AuthService;
import com.api.framework.services.UserService;
import com.api.framework.utils.ExtentReportManager;
import com.api.framework.utils.LoggingFilter;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;

/**
 * =========================================================
 * BaseTest.java
 * =========================================================
 * Parent class for all test classes.
 * All test classes MUST extend BaseTest to inherit:
 *
 *  @BeforeSuite  → Initialize REST Assured specs + Extent Reports
 *  @BeforeTest   → Login and retrieve auth token (API chaining)
 *  @BeforeMethod → Create an ExtentTest node for each test
 *  @AfterMethod  → Mark test pass/fail in report + attach logs
 *  @AfterSuite   → Flush Extent Report to disk
 *
 * Service instances are created here and shared with subclasses
 * via protected fields (protected = accessible in child classes).
 * =========================================================
 */
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);

    // ---- Service layer instances (shared across all test classes) ----
    protected AuthService authService;
    protected UserService userService;

    // =========================================================
    // SUITE SETUP (runs once before ALL tests in the entire suite)
    // =========================================================

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        log.info("========================================");
        log.info("  API Test Suite Starting");
        log.info("  Environment: {}", System.getProperty("env", "QA").toUpperCase());
        log.info("========================================");

        // 1. Initialize REST Assured base URI, request/response specs
        BaseApi.initializeSpecs();

        // 2. Initialize Extent Reports
        ExtentReportManager.initReports();

        log.info("Suite setup completed successfully.");
    }

    // =========================================================
    // TEST SETUP (runs once before each <test> tag in testng.xml)
    // =========================================================

    @BeforeTest(alwaysRun = true)
    public void testSetup() {
        // Initialize service classes
        authService = new AuthService();
        userService = new UserService();

        // ---- API CHAINING EXAMPLE ----
        // Login first to get token, then use it in all subsequent requests.
        // This demonstrates "API chaining": response data from one API
        // is used as input for the next API.
        log.info("Performing authentication (API chaining setup)...");
        try {
            Response loginResponse = authService.login();
            if (loginResponse.getStatusCode() == 200) {
                log.info("Authentication successful. Token ready for use.");
            } else {
                log.warn("Authentication returned status {}. " +
                        "Tests requiring auth token may fail.", loginResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Authentication skipped (may not be needed for all tests): {}", e.getMessage());
        }
    }

    // =========================================================
    // METHOD SETUP (runs before each @Test method)
    // =========================================================

    @BeforeMethod(alwaysRun = true)
    public void methodSetup(ITestResult result) {
        // Extract test name and description for the report
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();

        // Create a fresh ExtentTest node for this test
        ExtentReportManager.createTest(testName, description);

        log.info("------- Starting test: [{}] -------", testName);
    }

    // =========================================================
    // METHOD TEARDOWN (runs after each @Test method)
    // =========================================================

    @AfterMethod(alwaysRun = true)
    public void methodTeardown(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        String testName = result.getMethod().getMethodName();

        if (test != null) {
            // Attach last request/response logs to the report
            String requestLog  = LoggingFilter.getLastRequest();
            String responseLog = LoggingFilter.getLastResponse();

            if (!requestLog.equals("No request logged")) {
                test.info("<pre><b>REQUEST:</b>\n"  + escapeHtml(requestLog)  + "</pre>");
                test.info("<pre><b>RESPONSE:</b>\n" + escapeHtml(responseLog) + "</pre>");
            }

            // Mark test result in Extent Report
            switch (result.getStatus()) {
                case ITestResult.SUCCESS:
                    test.log(Status.PASS, "✅ Test PASSED: " + testName);
                    log.info("✅ Test PASSED: [{}]", testName);
                    break;

                case ITestResult.FAILURE:
                    test.log(Status.FAIL, "❌ Test FAILED: " + testName);
                    test.log(Status.FAIL, "Failure reason: " + result.getThrowable());
                    log.error("❌ Test FAILED: [{}] | Reason: {}", testName, result.getThrowable());
                    break;

                case ITestResult.SKIP:
                    test.log(Status.SKIP, "⏭️ Test SKIPPED: " + testName);
                    log.warn("⏭️ Test SKIPPED: [{}]", testName);
                    break;

                default:
                    log.warn("Unknown test result status for: [{}]", testName);
            }
        }

        // Clear logging filter thread-local data
        LoggingFilter.clear();

        // Remove test from ThreadLocal (cleanup for parallel execution)
        ExtentReportManager.removeTest();

        log.info("------- Completed test: [{}] -------\n", testName);
    }

    // =========================================================
    // SUITE TEARDOWN (runs once after ALL tests complete)
    // =========================================================

    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        // CRITICAL: Flush report to disk — without this, the HTML file is empty
        ExtentReportManager.flushReports();
        log.info("========================================");
        log.info("  API Test Suite Completed");
        log.info("  Extent Report saved to: reports/");
        log.info("========================================");
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    /**
     * Logs a step in both Log4j and Extent Report.
     * Use this in test methods instead of calling them separately.
     */
    protected void logStep(String message) {
        log.info("  STEP: {}", message);
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.info("📌 " + message);
        }
    }

    /**
     * Escapes HTML characters in log strings to display safely in Extent Report.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
