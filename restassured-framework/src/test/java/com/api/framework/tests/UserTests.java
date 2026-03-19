package com.api.framework.tests;

import com.api.framework.constants.ApiConstants;
import com.api.framework.payloads.*;
import com.api.framework.utils.JsonUtils;
import com.api.framework.utils.RandomDataGenerator;
import com.api.framework.utils.RetryAnalyzer;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * =========================================================
 * UserTests.java
 * =========================================================
 * Comprehensive test class for User CRUD API operations.
 * Demonstrates:
 *   ✅ Basic CRUD test cases
 *   ✅ Status code + response body validation
 *   ✅ JSON Schema validation
 *   ✅ Data-driven testing (from testdata.json)
 *   ✅ API chaining (create → verify → update → delete)
 *   ✅ Negative test cases
 *   ✅ Retry analyzer integration
 * =========================================================
 */
public class UserTests extends BaseTest {

    // =========================================================
    // GET USER TESTS
    // =========================================================

    /**
     * TC_USER_001
     * Verify GET /api/users returns 200 and user list.
     */
    @Test(
        description = "Verify GET all users returns 200 and non-empty user list",
        groups = {"smoke", "users"}
    )
    public void testGetAllUsers() {
        logStep("Sending GET /api/users?page=1");
        Response response = userService.getAllUsers(1);

        logStep("Validating status code is 200");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for GET all users");

        logStep("Validating response has 'data' array");
        List<?> userList = response.jsonPath().getList("data");
        Assert.assertNotNull(userList, "User list should not be null");
        Assert.assertFalse(userList.isEmpty(), "User list should not be empty");

        logStep("Validating pagination metadata");
        int page = response.jsonPath().getInt("page");
        Assert.assertEquals(page, 1, "Page number should match requested page");

        logStep("GET all users test passed. Users on page: " + userList.size());
    }

    /**
     * TC_USER_002
     * Verify GET /api/users/{id} returns correct user — with JSON schema validation.
     */
    @Test(
        description = "Verify GET single user returns 200 and valid schema",
        groups = {"smoke", "users"},
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testGetUserById() {
        int userId = 2;

        logStep("Sending GET /api/users/" + userId);
        Response response = userService.getUserById(userId);

        logStep("Validating status code is 200");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for valid user ID");

        logStep("Validating response body fields");
        GetUserResponse userResponse = JsonUtils.fromResponse(response, GetUserResponse.class);

        Assert.assertNotNull(userResponse.getData(), "Data field should not be null");
        Assert.assertEquals(userResponse.getData().getId(), userId,
                "Returned user ID should match requested ID");
        Assert.assertNotNull(userResponse.getData().getEmail(),
                "Email should be present");
        Assert.assertNotNull(userResponse.getData().getFirstName(),
                "First name should be present");

        logStep("Performing JSON Schema validation");
        response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        ApiConstants.SCHEMAS_DIR + "get-user-schema.json"));

        logStep("GET single user test passed. User: " + userResponse.getData().getFirstName()
                + " " + userResponse.getData().getLastName());
    }

    /**
     * TC_USER_003
     * Verify GET /api/users/{id} with non-existent ID returns 404.
     */
    @Test(
        description = "Verify GET non-existent user returns 404",
        groups = {"regression", "users", "negative"}
    )
    public void testGetNonExistentUser() {
        logStep("Sending GET /api/users/9999 (non-existent ID)");
        Response response = userService.getNonExistentUser();

        logStep("Validating status code is 404");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_NOT_FOUND,
                "Expected 404 Not Found for non-existent user ID");

        logStep("Validating response body is empty");
        String body = response.getBody().asString();
        Assert.assertEquals(body.trim(), "{}",
                "Response body should be empty JSON object for 404");

        logStep("Negative GET test passed (404 confirmed).");
    }

    // =========================================================
    // CREATE USER TESTS
    // =========================================================

    /**
     * TC_USER_004
     * Verify POST /api/users creates a user and returns 201.
     * Includes JSON schema validation.
     */
    @Test(
        description = "Verify POST create user returns 201 with id and createdAt",
        groups = {"smoke", "users"},
        retryAnalyzer = RetryAnalyzer.class
    )
    public void testCreateUser() {
        // Use RandomDataGenerator for unique test data each run
        String name = RandomDataGenerator.getFullName();
        String job  = RandomDataGenerator.getJobTitle();

        logStep("Creating user: name=[" + name + "], job=[" + job + "]");
        Response response = userService.createUser(name, job);

        logStep("Validating status code is 201 Created");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
                "Expected 201 Created for new user");

        logStep("Deserializing response into CreateUserResponse POJO");
        CreateUserResponse createdUser = JsonUtils.fromResponse(response, CreateUserResponse.class);

        logStep("Validating returned fields match request payload");
        Assert.assertEquals(createdUser.getName(), name,
                "Returned name should match sent name");
        Assert.assertEquals(createdUser.getJob(), job,
                "Returned job should match sent job");
        Assert.assertNotNull(createdUser.getId(),
                "ID should be auto-generated and returned");
        Assert.assertFalse(createdUser.getId().isEmpty(),
                "ID should not be empty");
        Assert.assertNotNull(createdUser.getCreatedAt(),
                "createdAt timestamp should be present");

        logStep("Performing JSON Schema validation on create response");
        response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                        ApiConstants.SCHEMAS_DIR + "create-user-schema.json"));

        logStep("Create user test passed. Generated ID: " + createdUser.getId());
    }

    /**
     * TC_USER_005 — Data-Driven Test
     * Verify POST /api/users works for multiple payloads from testdata.json.
     */
    @Test(
        description = "Data-driven: Create multiple users from testdata.json",
        groups = {"regression", "users"},
        dataProvider = "userDataProvider"
    )
    public void testCreateUserDataDriven(String name, String job, int expectedStatus) {
        logStep("Data-driven create: name=[" + name + "], job=[" + job + "]");
        Response response = userService.createUser(name, job);

        logStep("Validating status code is " + expectedStatus);
        Assert.assertEquals(response.getStatusCode(), expectedStatus,
                "Status code mismatch for data-driven test");

        if (expectedStatus == ApiConstants.STATUS_CREATED) {
            String id = response.jsonPath().getString("id");
            Assert.assertNotNull(id, "Created user should have an ID");
            logStep("User created with ID: " + id);
        }
    }

    // =========================================================
    // UPDATE USER TESTS
    // =========================================================

    /**
     * TC_USER_006
     * Verify PUT /api/users/{id} updates user and returns 200.
     */
    @Test(
        description = "Verify PUT update user returns 200 with updatedAt field",
        groups = {"smoke", "users"}
    )
    public void testUpdateUser() {
        int userId      = 2;
        String newName  = RandomDataGenerator.getFullName();
        String newJob   = RandomDataGenerator.getJobTitle();

        logStep("Updating user ID=" + userId + " → name=[" + newName + "], job=[" + newJob + "]");

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name(newName)
                .job(newJob)
                .build();

        Response response = userService.updateUser(userId, updateRequest);

        logStep("Validating status code is 200 OK");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for update");

        logStep("Deserializing response into UpdateUserResponse POJO");
        UpdateUserResponse updatedUser = JsonUtils.fromResponse(response, UpdateUserResponse.class);

        Assert.assertEquals(updatedUser.getName(), newName,
                "Updated name should match the request");
        Assert.assertEquals(updatedUser.getJob(), newJob,
                "Updated job should match the request");
        Assert.assertNotNull(updatedUser.getUpdatedAt(),
                "updatedAt timestamp should be present after update");

        logStep("Update user test passed. updatedAt: " + updatedUser.getUpdatedAt());
    }

    /**
     * TC_USER_007
     * Verify PATCH /api/users/{id} — partial update (only job field).
     */
    @Test(
        description = "Verify PATCH user with partial payload returns 200",
        groups = {"regression", "users"}
    )
    public void testPatchUser() {
        int userId   = 2;
        String newJob = "Principal " + RandomDataGenerator.getJobTitle();

        logStep("Patching user ID=" + userId + " with only job=[" + newJob + "]");

        // Name is null → @JsonInclude(NON_NULL) ensures it's not sent in body
        UpdateUserRequest patchRequest = UpdateUserRequest.builder()
                .job(newJob)
                .build();

        Response response = userService.patchUser(userId, patchRequest);

        logStep("Validating status code is 200 OK");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
                "Expected 200 OK for PATCH");

        String returnedJob = response.jsonPath().getString("job");
        Assert.assertEquals(returnedJob, newJob, "Patched job should match request");

        logStep("PATCH user test passed.");
    }

    // =========================================================
    // DELETE USER TESTS
    // =========================================================

    /**
     * TC_USER_008
     * Verify DELETE /api/users/{id} returns 204 No Content.
     */
    @Test(
        description = "Verify DELETE user returns 204 No Content",
        groups = {"smoke", "users"}
    )
    public void testDeleteUser() {
        int userId = 2;

        logStep("Sending DELETE /api/users/" + userId);
        Response response = userService.deleteUser(userId);

        logStep("Validating status code is 204 No Content");
        Assert.assertEquals(response.getStatusCode(), ApiConstants.STATUS_NO_CONTENT,
                "Expected 204 No Content after delete");

        logStep("Validating response body is empty");
        Assert.assertTrue(response.getBody().asString().isEmpty(),
                "DELETE response body should be empty");

        logStep("Delete user test passed.");
    }

    // =========================================================
    // API CHAINING TEST
    // =========================================================

    /**
     * TC_USER_009 — API CHAINING
     * Full lifecycle: Create user → Verify → Update → Delete
     * Response data from each step is used in the next step.
     */
    @Test(
        description = "API Chaining: Create → Verify → Update → Delete user lifecycle",
        groups = {"regression", "users", "e2e"}
    )
    public void testUserLifecycleApiChaining() {

        // ---- STEP 1: Create user ----
        String name = RandomDataGenerator.getFullName();
        String job  = RandomDataGenerator.getJobTitle();

        logStep("[Chain 1/4] Creating user: " + name);
        Response createResponse = userService.createUser(name, job);

        Assert.assertEquals(createResponse.getStatusCode(), ApiConstants.STATUS_CREATED,
                "Create should return 201");

        // Extract the generated ID from create response (API chaining!)
        String createdId = JsonUtils.extractField(createResponse, "id");
        logStep("[Chain 1/4] User created with ID: " + createdId);
        Assert.assertNotNull(createdId, "Created ID should not be null");

        // ---- STEP 2: Use a known ID to GET the user ----
        // (reqres.in doesn't persist created users, so we use an existing ID)
        int verifyId = 2;
        logStep("[Chain 2/4] Verifying user exists at ID: " + verifyId);
        Response getResponse = userService.getUserById(verifyId);

        Assert.assertEquals(getResponse.getStatusCode(), ApiConstants.STATUS_OK,
                "GET should return 200");

        // Extract name from GET response for use in update
        String existingFirstName = JsonUtils.extractField(getResponse, "data.first_name");
        logStep("[Chain 2/4] Retrieved user: " + existingFirstName);

        // ---- STEP 3: Update the user ----
        String updatedName = existingFirstName + " Updated";
        String updatedJob  = "Senior " + RandomDataGenerator.getJobTitle();

        logStep("[Chain 3/4] Updating user ID=" + verifyId + " → " + updatedName);
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name(updatedName)
                .job(updatedJob)
                .build();
        Response updateResponse = userService.updateUser(verifyId, updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), ApiConstants.STATUS_OK,
                "Update should return 200");

        // Verify the update response has updatedAt
        String updatedAt = JsonUtils.extractField(updateResponse, "updatedAt");
        Assert.assertNotNull(updatedAt, "updatedAt should be present");
        logStep("[Chain 3/4] Update confirmed. updatedAt: " + updatedAt);

        // ---- STEP 4: Delete the user ----
        logStep("[Chain 4/4] Deleting user ID=" + verifyId);
        Response deleteResponse = userService.deleteUser(verifyId);

        Assert.assertEquals(deleteResponse.getStatusCode(), ApiConstants.STATUS_NO_CONTENT,
                "Delete should return 204");

        logStep("[Chain 4/4] User lifecycle complete (Create → Verify → Update → Delete).");
    }

    // =========================================================
    // DATA PROVIDER
    // =========================================================

    /**
     * DataProvider that reads test payloads from testdata.json.
     * Returns Object[][] where each row = one test iteration.
     *
     * Structure in testdata.json:
     *   "users": [ { "name": "...", "job": "...", "expectedStatusCode": 201 }, ... ]
     */
    @DataProvider(name = "userDataProvider", parallel = false)
    public Object[][] userDataProvider() {
        logStep("Loading user test data from testdata.json");

        // Read the "users" array from testdata.json as a List of Maps
        List<Map<String, Object>> users = (List<Map<String, Object>>)
                JsonUtils.readJsonAsMap(ApiConstants.TEST_DATA_FILE).get("users");

        Object[][] data = new Object[users.size()][3];
        for (int i = 0; i < users.size(); i++) {
            Map<String, Object> user = users.get(i);
            data[i][0] = user.get("name");
            data[i][1] = user.get("job");
            data[i][2] = user.get("expectedStatusCode");
        }

        log.info("Loaded {} test records from testdata.json", data.length);
        return data;
    }
}
