package com.api.framework.services;

import com.api.framework.base.BaseApi;
import com.api.framework.constants.ApiConstants;
import com.api.framework.payloads.CreateUserRequest;
import com.api.framework.payloads.UpdateUserRequest;
import com.api.framework.utils.ConfigReader;
import io.restassured.response.Response;

/**
 * =========================================================
 * UserService.java
 * =========================================================
 * Service class encapsulating all User API operations.
 * Maps to the /api/users endpoint.
 *
 * Supported operations:
 *   GET    /api/users          → list all users (paginated)
 *   GET    /api/users/{id}     → get single user by ID
 *   POST   /api/users          → create new user
 *   PUT    /api/users/{id}     → update user (full replace)
 *   DELETE /api/users/{id}     → delete user
 *
 * Design: Returns raw Response objects so test classes
 * can perform assertions — service classes do NOT assert.
 * =========================================================
 */
public class UserService extends BaseApi {

    // Endpoint path loaded from config.properties
    private final String usersEndpoint = ConfigReader.get(ApiConstants.PROP_ENDPOINT_USERS);

    // =========================================================
    // GET METHODS
    // =========================================================

    /**
     * Gets a paginated list of users.
     * Calls: GET /api/users?page={pageNumber}
     *
     * @param pageNumber  Page number (1-based)
     * @return REST Assured Response with user list
     */
    public Response getAllUsers(int pageNumber) {
        log.info("Fetching all users - page: {}", pageNumber);

        return getBaseSpec()
                .queryParam("page", pageNumber)
                .when()
                    .get(usersEndpoint)
                .then()
                    .extract().response();
    }

    /**
     * Gets all users on page 1 (convenience method).
     */
    public Response getAllUsers() {
        return getAllUsers(1);
    }

    /**
     * Gets a single user by their ID.
     * Calls: GET /api/users/{id}
     *
     * @param userId  The user's numeric ID
     * @return REST Assured Response with user details
     */
    public Response getUserById(int userId) {
        log.info("Fetching user with ID: {}", userId);

        return getBaseSpec()
                .when()
                    .get(usersEndpoint + "/" + userId)
                .then()
                    .extract().response();
    }

    /**
     * Gets a user that doesn't exist (for negative testing).
     * Calls: GET /api/users/9999
     *
     * @return REST Assured Response (expected: 404)
     */
    public Response getNonExistentUser() {
        log.info("Fetching non-existent user (negative test)");
        return getUserById(9999);
    }

    // =========================================================
    // POST METHODS
    // =========================================================

    /**
     * Creates a new user with the given request body.
     * Calls: POST /api/users
     *
     * @param createUserRequest  POJO with name and job fields
     * @return REST Assured Response (expected: 201 Created)
     */
    public Response createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user: name=[{}], job=[{}]",
                createUserRequest.getName(), createUserRequest.getJob());

        return getBaseSpec()
                .body(createUserRequest)
                .when()
                    .post(usersEndpoint)
                .then()
                    .extract().response();
    }

    /**
     * Creates a user using explicit name and job parameters.
     * Convenience overload — builds the POJO internally.
     *
     * @param name  User's full name
     * @param job   User's job title
     * @return REST Assured Response
     */
    public Response createUser(String name, String job) {
        CreateUserRequest request = CreateUserRequest.builder()
                .name(name)
                .job(job)
                .build();
        return createUser(request);
    }

    // =========================================================
    // PUT METHODS
    // =========================================================

    /**
     * Fully updates an existing user (replaces all fields).
     * Calls: PUT /api/users/{id}
     *
     * @param userId             The user's ID to update
     * @param updateUserRequest  POJO with updated name and job
     * @return REST Assured Response (expected: 200 OK)
     */
    public Response updateUser(int userId, UpdateUserRequest updateUserRequest) {
        log.info("Updating user ID: {} → name=[{}], job=[{}]",
                userId, updateUserRequest.getName(), updateUserRequest.getJob());

        return getBaseSpec()
                .body(updateUserRequest)
                .when()
                    .put(usersEndpoint + "/" + userId)
                .then()
                    .extract().response();
    }

    /**
     * Partially updates a user using PATCH.
     * Calls: PATCH /api/users/{id}
     *
     * @param userId             The user's ID
     * @param updateUserRequest  Partial update payload (null fields are skipped via @JsonInclude)
     * @return REST Assured Response (expected: 200 OK)
     */
    public Response patchUser(int userId, UpdateUserRequest updateUserRequest) {
        log.info("Patching user ID: {}", userId);

        return getBaseSpec()
                .body(updateUserRequest)
                .when()
                    .patch(usersEndpoint + "/" + userId)
                .then()
                    .extract().response();
    }

    // =========================================================
    // DELETE METHODS
    // =========================================================

    /**
     * Deletes a user by ID.
     * Calls: DELETE /api/users/{id}
     *
     * @param userId  The user's ID to delete
     * @return REST Assured Response (expected: 204 No Content)
     */
    public Response deleteUser(int userId) {
        log.info("Deleting user with ID: {}", userId);

        return getBaseSpec()
                .when()
                    .delete(usersEndpoint + "/" + userId)
                .then()
                    .extract().response();
    }

    // =========================================================
    // AUTHENTICATED METHODS (examples using Bearer token)
    // =========================================================

    /**
     * Gets user list using authenticated request.
     * Use this if the endpoint requires a Bearer token.
     *
     * @param pageNumber  Page number
     * @return REST Assured Response
     */
    public Response getAllUsersAuthenticated(int pageNumber) {
        log.info("Fetching users (authenticated) - page: {}", pageNumber);

        return getAuthenticatedSpec()
                .queryParam("page", pageNumber)
                .when()
                    .get(usersEndpoint)
                .then()
                    .extract().response();
    }
}
