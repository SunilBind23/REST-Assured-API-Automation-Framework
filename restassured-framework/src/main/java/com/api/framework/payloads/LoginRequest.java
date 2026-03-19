package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * LoginRequest.java
 * =========================================================
 * POJO representing the request body for POST /api/login
 *
 * Jackson annotations:
 *   @JsonProperty     → maps Java field to JSON key
 *   @JsonIgnoreProperties → ignores unknown JSON fields
 *
 * Lombok annotations:
 *   @Data             → generates getters, setters, toString, equals, hashCode
 *   @Builder          → enables builder pattern: LoginRequest.builder().email("x").build()
 *   @NoArgsConstructor → default constructor (required by Jackson)
 *   @AllArgsConstructor → all-args constructor (used by @Builder)
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;
}
