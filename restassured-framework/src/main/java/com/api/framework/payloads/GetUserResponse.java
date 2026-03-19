package com.api.framework.payloads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =========================================================
 * GetUserResponse.java
 * =========================================================
 * POJO representing the full response from GET /api/users/{id}
 *
 * Example:
 * {
 *   "data": { "id": 2, "email": "...", ... },
 *   "support": { "url": "...", "text": "..." }
 * }
 * =========================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetUserResponse {

    @JsonProperty("data")
    private UserData data;

    @JsonProperty("support")
    private SupportData support;

    // ---- Inner class for the "support" node ----
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupportData {
        @JsonProperty("url")
        private String url;

        @JsonProperty("text")
        private String text;
    }
}
