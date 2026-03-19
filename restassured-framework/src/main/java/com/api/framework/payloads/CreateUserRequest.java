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
 * CreateUserRequest.java
 * =========================================================
 * POJO representing the request body for POST /api/users
 *
 * Example request:
 * {
 *   "name": "John Doe",
 *   "job": "Software Engineer"
 * }
 *
 * @JsonInclude(NON_NULL) ensures null fields are NOT sent
 * in the JSON body — keeps requests clean.
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("job")
    private String job;
}
