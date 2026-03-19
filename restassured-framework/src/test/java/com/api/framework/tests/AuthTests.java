package com.api.framework.tests;

import com.api.framework.constants.ApiConstants;
import com.api.framework.utils.ConfigReader;
import com.api.framework.utils.RetryAnalyzer;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * =========================================================
 * AuthTests.java
 * =========================================================
 * Test class covering all Authentication API scenarios.
 * Extends BaseTest to inherit setup/teardown lifecycle.
 *
 * Test coverage:
 *  TC_AUTH_001 → Successful login with valid credentials
 *  TC_AUTH_002 → Failed login with invalid credentials
 *  TC_AUTH_003 → Successful user registration
 *  TC_AUTH_004 → Registration without password (negative)
 *  TC_AUTH_005 → Verify token is stored after login (API chaining)
 * =========================================================
 */
public class AuthTests extends BaseTest {

    // =========================================================
    // LOGIN TESTS
    // =========================================================

    /**
     * TC_AUTH_001
     * Verify login succeeds with valid credentials from config.
     */
    @Test(
        description = "Verify successful login returns 200 and a non-empty token",
        groups = {"smoke", "auth"},
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testSuccessfulLogin() {
        logStep("Sending POST /api/login with valid credentials");
        Response response = authService.login();

        logStep("Asserting status code is 200");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for valid login");

        logStep("Asserting token is present in response");
        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Token should not be null");
        Assert.assertFalse(token.isEmpty(), "Token should not be empty");

        logStep("Login test passed. Token length: " + token.length());
    }

    /**
     * TC_AUTH_002
     * Verify login fails with invalid password — expects 400.
     */
    @Test(
        description = "Verify login with wrong password returns 400 and error message",
        groups = {"regression", "auth", "negative"}
    )
    public void testLoginWithInvalidCredentials() {
        logStep("Sending POST /api/login with invalid credentials");
        Response response = authService.login("invalid@notexist.com", "wrongpassword");

        logStep("Asserting status code is 400");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
                "Expected 400 Bad Request for invalid credentials");

        logStep("Asserting error message is present");
        String errorMsg = response.jsonPath().getString("error");
        Assert.assertNotNull(errorMsg, "Error message should be present in failed login response");
        Assert.assertFalse(errorMsg.isEmpty(), "Error message should not be empty");

        logStep("Negative login test passed. Error: " + errorMsg);
    }

    // =========================================================
    // REGISTER TESTS
    // =========================================================

    /**
     * TC_AUTH_003
     * Verify registration succeeds with valid email and password.
     */
    @Test(
        description = "Verify user registration with valid credentials returns 200 and token",
        groups = {"regression", "auth"}
    )
    public void testSuccessfulRegistration() {
        // Using a known email that reqres.in accepts for registration
        String email    = "eve.holt@reqres.in";
        String password = "pistol";

        logStep("Sending POST /api/register with email: " + email);
        Response response = authService.register(email, password);

        logStep("Asserting status code is 200");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for valid registration");

        logStep("Asserting response contains an id or token");
        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Registration should return a token");

        logStep("Registration test passed.");
    }

    /**
     * TC_AUTH_004
     * Verify registration fails when password is missing — negative test.
     */
    @Test(
        description = "Verify registration without password returns 400 and error",
        groups = {"regression", "auth", "negative"}
    )
    public void testRegistrationWithoutPassword() {
        String email = "sydney@fife.com";

        logStep("Sending POST /api/register without password for: " + email);
        Response response = authService.registerWithoutPassword(email);

        logStep("Asserting status code is 400");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
                "Expected 400 Bad Request when password is missing");

        logStep("Asserting error message is present");
        String error = response.jsonPath().getString("error");
        Assert.assertNotNull(error, "Error field should exist");
        Assert.assertEquals(error, "Missing password",
                "Error message should say 'Missing password'");

        logStep("Negative registration test passed. Error: " + error);
    }

    /**
     * TC_AUTH_005
     * Verify the auth token is stored in BaseApi after login (API chaining readiness).
     */
    @Test(
        description = "Verify auth token is stored in BaseApi after successful login",
        groups = {"smoke", "auth"},
        dependsOnMethods = {"testSuccessfulLogin"}
    )
    public void testTokenStoredAfterLogin() {
        logStep("Retrieving stored auth token from BaseApi");
        String storedToken = com.api.framework.base.BaseApi.getAuthToken();

        logStep("Asserting stored token is not null/empty");
        Assert.assertNotNull(storedToken,
                "Auth token should be stored in BaseApi after login");
        Assert.assertFalse(storedToken.isEmpty(),
                "Stored auth token should not be empty");

        logStep("Token stored successfully. Ready for API chaining. Length: " + storedToken.length());
    }
}
