package com.api.framework.constants;

/**
 * =========================================================
 * ApiConstants.java
 * =========================================================
 * Central place for all constant values used across the framework.
 * This avoids hardcoding magic strings/numbers in test classes.
 *
 * Usage: ApiConstants.STATUS_OK  →  200
 * =========================================================
 */
public class ApiConstants {

    // ---- Private constructor to prevent instantiation ----
    private ApiConstants() {}

    // =============================================
    // HTTP Status Codes
    // =============================================
    public static final int STATUS_OK           = 200;   // GET, PUT success
    public static final int STATUS_CREATED      = 201;   // POST success
    public static final int STATUS_NO_CONTENT   = 204;   // DELETE success
    public static final int STATUS_BAD_REQUEST  = 400;   // Invalid request
    public static final int STATUS_UNAUTHORIZED = 401;   // Not authenticated
    public static final int STATUS_NOT_FOUND    = 404;   // Resource not found
    public static final int STATUS_SERVER_ERROR = 500;   // Internal server error

    // =============================================
    // Content Types
    // =============================================
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML  = "application/xml";

    // =============================================
    // Header Names
    // =============================================
    public static final String HEADER_CONTENT_TYPE  = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT        = "Accept";

    // =============================================
    // Auth Token Prefix
    // =============================================
    public static final String BEARER_PREFIX = "Bearer ";

    // =============================================
    // Environment Names
    // =============================================
    public static final String ENV_QA   = "qa";
    public static final String ENV_UAT  = "uat";
    public static final String ENV_PROD = "prod";

    // =============================================
    // Config Property Keys
    // =============================================
    public static final String PROP_BASE_URL          = "base.url";
    public static final String PROP_ENDPOINT_USERS    = "endpoint.users";
    public static final String PROP_ENDPOINT_AUTH     = "endpoint.auth";
    public static final String PROP_ENDPOINT_REGISTER = "endpoint.register";
    public static final String PROP_AUTH_EMAIL        = "auth.email";
    public static final String PROP_AUTH_PASSWORD     = "auth.password";
    public static final String PROP_CONN_TIMEOUT      = "connection.timeout";
    public static final String PROP_READ_TIMEOUT      = "read.timeout";

    // =============================================
    // Miscellaneous
    // =============================================
    public static final String TEST_DATA_FILE   = "testdata.json";
    public static final String REPORTS_DIR      = "reports/";
    public static final String LOGS_DIR         = "logs/";
    public static final String SCHEMAS_DIR      = "schemas/";
}
