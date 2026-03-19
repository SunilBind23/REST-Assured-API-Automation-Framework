package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * CreateUserResponse.java
 * =========================================================
 * POJO representing the response body from POST /api/users
 *
 * Example response:
 * {
 *   "name": "John Doe",
 *   "job": "Software Engineer",
 *   "id": "123",
 *   "createdAt": "2024-01-15T10:30:00.000Z"
 * }
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("job")
    private String job;

    @JsonProperty("id")
    private String id;

    @JsonProperty("createdAt")
    private String createdAt;
}
