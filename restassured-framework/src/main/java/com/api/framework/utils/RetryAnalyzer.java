package com.api.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * =========================================================
 * RetryAnalyzer.java
 * =========================================================
 * Automatically retries flaky/failed tests up to MAX_RETRY times.
 *
 * How to use:
 *   Option 1 - On a specific test:
 *     @Test(retryAnalyzer = RetryAnalyzer.class)
 *
 *   Option 2 - Globally via RetryListener (preferred):
 *     Add RetryListener to testng.xml listeners section
 *
 * The retry count resets per test method (each test gets MAX_RETRY retries).
 * =========================================================
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);

    // Maximum number of retry attempts per test
    private static final int MAX_RETRY = 2;

    // ThreadLocal ensures each thread (parallel execution) has its own counter
    private ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);

    /**
     * Called by TestNG whenever a test fails.
     * Return true to retry the test; false to mark it as failed.
     *
     * @param result  The result of the failed test
     * @return true if test should be retried, false otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
        int currentCount = retryCount.get();

        if (currentCount < MAX_RETRY) {
            retryCount.set(currentCount + 1);
            log.warn("⚠️ Test FAILED: [{}] | Retry attempt [{}/{}]",
                    result.getName(), retryCount.get(), MAX_RETRY);
            return true;  // Retry the test
        }

        // Max retries reached — mark test as failed
        log.error("❌ Test FAILED after {} retries: [{}]", MAX_RETRY, result.getName());
        retryCount.remove(); // Clean up ThreadLocal to prevent memory leaks
        return false;
    }
}
