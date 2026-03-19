package com.api.framework.base;

import com.api.framework.constants.ApiConstants;
import com.api.framework.utils.ConfigReader;
import com.api.framework.utils.LoggingFilter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/**
 * =========================================================
 * BaseApi.java
 * =========================================================
 * The foundation of the API framework.
 * All service classes (UserService, AuthService) extend this class.
 *
 * Responsibilities:
 *  - Set up the base URI from config
 *  - Build default RequestSpecification (common headers, content-type)
 *  - Build default ResponseSpecification (expected content-type)
 *  - Provide authenticated RequestSpecification (with Bearer token)
 *  - Attach the custom LoggingFilter for request/response logging
 *
 * Why use RequestSpecification?
 *  - Avoids repeating common setup (headers, base URL) in every request
 *  - Central place to update headers/auth for all API calls
 * =========================================================
 */
public class BaseApi {

    protected static final Logger log = LogManager.getLogger(BaseApi.class);

    // Shared specs - built once, reused across all tests
    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;

    // Stores auth token retrieved from login (used for authenticated requests)
    private static volatile String authToken;

    /**
     * Initializes the base REST Assured configuration.
     * Called once before any tests run (from BaseTest @BeforeSuite).
     */
    public static void initializeSpecs() {
        String baseUrl = ConfigReader.getBaseUrl();
        log.info("Initializing REST Assured base URI: {}", baseUrl);

        // Set global base URI
        RestAssured.baseURI = baseUrl;

        // Disable REST Assured's built-in logging (we use our custom filter)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);

        // Build default RequestSpecification
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)           // All requests send JSON
                .setAccept(ContentType.JSON)                // All requests accept JSON
                .addHeader("x-api-client", "RestAssured-Framework")  // Custom client header
                .addFilter(new LoggingFilter())             // Attach custom logger
                .log(LogDetail.NONE)                        // Disable built-in logging
                .build();

        // Build default ResponseSpecification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)        // All responses should be JSON
                .build();

        log.info("REST Assured specs initialized successfully.");
    }

    /**
     * Returns a RequestSpecification with Bearer token attached.
     * Use this for endpoints that require authentication.
     */
    protected RequestSpecification getAuthenticatedSpec() {
        if (authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException(
                "Auth token is not set. Call AuthService.login() first.");
        }
        return given()
                .spec(requestSpec)
                .header(ApiConstants.HEADER_AUTHORIZATION,
                        ApiConstants.BEARER_PREFIX + authToken);
    }

    /**
     * Returns the base RequestSpecification (no auth token).
     * Use for public endpoints (login, register, etc.)
     */
    protected RequestSpecification getBaseSpec() {
        return given().spec(requestSpec);
    }

    /**
     * Stores the auth token for reuse across all service classes.
     * Thread-safe: marked volatile for cross-thread visibility.
     *
     * @param token  The Bearer token from login response
     */
    public static synchronized void setAuthToken(String token) {
        authToken = token;
        log.info("Auth token stored successfully (length: {} chars).", token.length());
    }

    /**
     * Returns the stored auth token.
     */
    public static String getAuthToken() {
        return authToken;
    }

    /**
     * Returns the default RequestSpecification.
     * Accessible by subclasses and test classes.
     */
    public static RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    /**
     * Returns the default ResponseSpecification.
     */
    public static ResponseSpecification getResponseSpec() {
        return responseSpec;
    }
}
