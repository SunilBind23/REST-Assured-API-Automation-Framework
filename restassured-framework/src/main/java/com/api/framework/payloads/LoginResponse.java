package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * LoginResponse.java
 * =========================================================
 * POJO representing the response body from POST /api/login
 *
 * Example response:
 * {
 *   "token": "QpwL5tpe83ilfN2..."
 * }
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    @JsonProperty("token")
    private String token;

    // Error field returned on failed login
    @JsonProperty("error")
    private String error;
}
