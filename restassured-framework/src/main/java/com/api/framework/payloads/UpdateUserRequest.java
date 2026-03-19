package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * UpdateUserRequest.java
 * =========================================================
 * POJO representing the request body for PUT /api/users/{id}
 *
 * Example request:
 * {
 *   "name": "John Updated",
 *   "job": "Senior Engineer"
 * }
 *
 * @JsonInclude(NON_NULL) - only includes non-null fields,
 * enabling partial updates (PATCH-like behavior via PUT).
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("job")
    private String job;
}
