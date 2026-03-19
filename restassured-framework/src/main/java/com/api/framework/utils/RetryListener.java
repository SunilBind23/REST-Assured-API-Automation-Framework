package com.api.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * =========================================================
 * RetryListener.java
 * =========================================================
 * A TestNG listener that automatically applies RetryAnalyzer
 * to ALL test methods — so you don't need to add
 * retryAnalyzer = RetryAnalyzer.class to every @Test annotation.
 *
 * Register it in testng.xml:
 *   <listeners>
 *     <listener class-name="com.api.framework.utils.RetryListener"/>
 *   </listeners>
 * =========================================================
 */
public class RetryListener implements IAnnotationTransformer {

    private static final Logger log = LogManager.getLogger(RetryListener.class);

    /**
     * Called by TestNG for each @Test annotation before the test runs.
     * We inject our RetryAnalyzer here.
     */
    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        // Only set if not already set by developer
        if (annotation.getRetryAnalyzer() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
            log.debug("RetryAnalyzer injected for method: [{}]",
                    testMethod != null ? testMethod.getName() : "unknown");
        }
    }
}
