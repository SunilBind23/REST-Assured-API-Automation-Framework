package com.api.framework.services;

import com.api.framework.base.BaseApi;
import com.api.framework.constants.ApiConstants;
import com.api.framework.payloads.LoginRequest;
import com.api.framework.payloads.LoginResponse;
import com.api.framework.utils.ConfigReader;
import com.api.framework.utils.JsonUtils;
import io.restassured.response.Response;

/**
 * =========================================================
 * AuthService.java
 * =========================================================
 * Service class encapsulating all Authentication API operations.
 * Extends BaseApi to inherit request/response specs.
 *
 * Responsibilities:
 *  - POST /api/login  → retrieve token
 *  - POST /api/register → register new user
 *  - Store the token in BaseApi for reuse by other services
 *
 * Design: Service Layer Pattern
 *  Tests call AuthService methods → AuthService calls REST Assured
 *  Tests never directly use given().when().then() — keeps tests clean.
 * =========================================================
 */
public class AuthService extends BaseApi {

    // Endpoint path loaded from config.properties
    private final String authEndpoint = ConfigReader.get(ApiConstants.PROP_ENDPOINT_AUTH);
    private final String registerEndpoint = ConfigReader.get(ApiConstants.PROP_ENDPOINT_REGISTER);

    // =========================================================
    // LOGIN
    // =========================================================

    /**
     * Performs login using credentials from config.properties.
     * On success, stores the token in BaseApi for reuse.
     *
     * @return Raw REST Assured Response (caller can validate status, body)
     */
    public Response login() {
        String email    = ConfigReader.get(ApiConstants.PROP_AUTH_EMAIL);
        String password = ConfigReader.get(ApiConstants.PROP_AUTH_PASSWORD);
        return login(email, password);
    }

    /**
     * Performs login with explicit credentials.
     * Use this for negative tests (e.g., wrong password).
     *
     * @param email     User email
     * @param password  User password
     * @return Raw REST Assured Response
     */
    public Response login(String email, String password) {
        log.info("Attempting login for email: [{}]", email);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        Response response = getBaseSpec()
                .body(loginRequest)
                .when()
                    .post(authEndpoint)
                .then()
                    .extract().response();

        // If login was successful (200), store the token for all other services
        if (response.getStatusCode() == ApiConstants.STATUS_OK) {
            LoginResponse loginResponse = JsonUtils.fromResponse(response, LoginResponse.class);
            if (loginResponse.getToken() != null) {
                BaseApi.setAuthToken(loginResponse.getToken());
                log.info("Login successful. Token stored.");
            }
        } else {
            log.warn("Login failed with status: {}. Token NOT stored.", response.getStatusCode());
        }

        return response;
    }

    /**
     * Performs login and directly returns the token string.
     * Convenience method when you just need the token.
     *
     * @return Bearer token string
     * @throws RuntimeException if login fails
     */
    public String loginAndGetToken() {
        Response response = login();
        if (response.getStatusCode() != ApiConstants.STATUS_OK) {
            throw new RuntimeException("Login failed. Status: " + response.getStatusCode()
                    + " Body: " + response.getBody().asString());
        }
        return BaseApi.getAuthToken();
    }

    // =========================================================
    // REGISTER
    // =========================================================

    /**
     * Registers a new user.
     * Uses the same LoginRequest POJO (email + password).
     *
     * @param email     New user email
     * @param password  New user password
     * @return Raw REST Assured Response
     */
    public Response register(String email, String password) {
        log.info("Registering new user with email: [{}]", email);

        LoginRequest registerRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        return getBaseSpec()
                .body(registerRequest)
                .when()
                    .post(registerEndpoint)
                .then()
                    .extract().response();
    }

    /**
     * Attempts registration without a password (negative test).
     *
     * @param email  Email only — no password
     * @return Raw REST Assured Response (expected: 400)
     */
    public Response registerWithoutPassword(String email) {
        log.info("Attempting registration without password for: [{}]", email);

        // Only email in body — password intentionally omitted
        LoginRequest incompleteRequest = LoginRequest.builder()
                .email(email)
                .build();

        return getBaseSpec()
                .body(incompleteRequest)
                .when()
                    .post(registerEndpoint)
                .then()
                    .extract().response();
    }
}
