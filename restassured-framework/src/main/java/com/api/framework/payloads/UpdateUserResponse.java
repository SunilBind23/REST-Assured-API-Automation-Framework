package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * UpdateUserResponse.java
 * =========================================================
 * POJO representing the response from PUT /api/users/{id}
 *
 * Example:
 * {
 *   "name": "John Updated",
 *   "job": "Senior Engineer",
 *   "updatedAt": "2024-01-15T10:45:00.000Z"
 * }
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("job")
    private String job;

    @JsonProperty("updatedAt")
    private String updatedAt;
}
