package com.api.framework.utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * =========================================================
 * LoggingFilter.java
 * =========================================================
 * Custom REST Assured Filter that logs full HTTP request
 * and response details using Log4j.
 *
 * Attach to a request like:
 *   given().filter(new LoggingFilter()) ...
 *
 * Or add to the default RequestSpecification in BaseTest
 * to apply to all requests automatically.
 *
 * Also stores the last request/response for Extent Reports.
 * =========================================================
 */
public class LoggingFilter implements Filter {

    private static final Logger log = LogManager.getLogger(LoggingFilter.class);

    // Thread-local storage for last request/response details
    // (for attaching to Extent Report in parallel execution)
    private static ThreadLocal<String> lastRequest  = new ThreadLocal<>();
    private static ThreadLocal<String> lastResponse = new ThreadLocal<>();

    /**
     * This method is called by REST Assured for every HTTP request/response.
     */
    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        // --- Log Request ---
        String requestLog = buildRequestLog(requestSpec);
        log.info(requestLog);
        lastRequest.set(requestLog);

        // --- Execute the actual HTTP call ---
        Response response = ctx.next(requestSpec, responseSpec);

        // --- Log Response ---
        String responseLog = buildResponseLog(response);
        log.info(responseLog);
        lastResponse.set(responseLog);

        return response;
    }

    /**
     * Builds a formatted string of the HTTP request.
     */
    private String buildRequestLog(FilterableRequestSpecification request) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== HTTP REQUEST ==========\n");
        sb.append("Method  : ").append(request.getMethod()).append("\n");
        sb.append("URI     : ").append(request.getURI()).append("\n");

        if (request.getHeaders() != null) {
            sb.append("Headers : ").append(request.getHeaders()).append("\n");
        }
        if (request.getBody() != null) {
            sb.append("Body    : ").append(request.getBody()).append("\n");
        }
        sb.append("==================================");
        return sb.toString();
    }

    /**
     * Builds a formatted string of the HTTP response.
     */
    private String buildResponseLog(Response response) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== HTTP RESPONSE ==========\n");
        sb.append("Status  : ").append(response.getStatusCode())
          .append(" ").append(response.getStatusLine()).append("\n");
        sb.append("Time    : ").append(response.getTime()).append("ms\n");

        if (response.getHeaders() != null) {
            sb.append("Headers : ").append(response.getHeaders()).append("\n");
        }

        String body = response.getBody().asString();
        if (body != null && !body.isEmpty()) {
            // Truncate very long responses for readability
            sb.append("Body    : ")
              .append(body.length() > 2000 ? body.substring(0, 2000) + "...[TRUNCATED]" : body)
              .append("\n");
        }
        sb.append("===================================");
        return sb.toString();
    }

    // =========================================================
    // STATIC ACCESSORS (for Extent Report integration)
    // =========================================================

    /** Returns the last logged request string (thread-safe) */
    public static String getLastRequest() {
        return lastRequest.get() != null ? lastRequest.get() : "No request logged";
    }

    /** Returns the last logged response string (thread-safe) */
    public static String getLastResponse() {
        return lastResponse.get() != null ? lastResponse.get() : "No response logged";
    }

    /** Clears stored request/response (call after each test) */
    public static void clear() {
        lastRequest.remove();
        lastResponse.remove();
    }
}
