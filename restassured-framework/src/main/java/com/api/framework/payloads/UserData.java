package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * UserData.java
 * =========================================================
 * POJO representing the "data" object inside GET /api/users/{id}
 *
 * Example:
 * {
 *   "id": 2,
 *   "email": "janet.weaver@reqres.in",
 *   "first_name": "Janet",
 *   "last_name": "Weaver",
 *   "avatar": "https://reqres.in/img/faces/2-image.jpg"
 * }
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData {

    @JsonProperty("id")
    private int id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("avatar")
    private String avatar;
}
