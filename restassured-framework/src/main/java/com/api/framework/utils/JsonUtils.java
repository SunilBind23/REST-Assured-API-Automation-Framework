package com.api.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * =========================================================
 * JsonUtils.java
 * =========================================================
 * Utility class for all JSON-related operations:
 *  - Reading test data from JSON files
 *  - Serializing POJO to JSON string
 *  - Deserializing JSON string/Response to POJO
 *  - Extracting specific nodes from JSON
 *
 * Uses Jackson ObjectMapper under the hood.
 * =========================================================
 */
public class JsonUtils {

    private static final Logger log = LogManager.getLogger(JsonUtils.class);

    // Shared ObjectMapper - thread-safe after configuration
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);  // Pretty-print JSON

    // Private constructor - utility class, no instantiation
    private JsonUtils() {}

    // =========================================================
    // FILE READING METHODS
    // =========================================================

    /**
     * Reads a JSON file from the classpath (src/test/resources) and
     * deserializes it into the specified POJO class.
     *
     * @param fileName  e.g. "testdata.json"
     * @param valueType The class to deserialize into
     * @return Populated object of type T
     */
    public static <T> T readJsonFile(String fileName, Class<T> valueType) {
        try {
            InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (stream == null) {
                throw new RuntimeException("JSON file not found on classpath: " + fileName);
            }
            T result = objectMapper.readValue(stream, valueType);
            log.debug("Successfully read JSON file: [{}] as [{}]", fileName, valueType.getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("Failed to read JSON file: [{}]", fileName, e);
            throw new RuntimeException("Error reading JSON file: " + fileName, e);
        }
    }

    /**
     * Reads a JSON file into a generic Map<String, Object>.
     * Useful for accessing dynamic/unknown JSON structures.
     *
     * @param fileName  e.g. "testdata.json"
     * @return Map representation of JSON
     */
    public static Map<String, Object> readJsonAsMap(String fileName) {
        try {
            InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (stream == null) {
                throw new RuntimeException("JSON file not found: " + fileName);
            }
            Map<String, Object> result = objectMapper.readValue(stream,
                    new TypeReference<Map<String, Object>>() {});
            log.debug("Read JSON file [{}] as Map with {} keys", fileName, result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to read JSON as Map: [{}]", fileName, e);
            throw new RuntimeException("Error reading JSON as Map: " + fileName, e);
        }
    }

    /**
     * Reads a JSON array node by key from a file and returns as a List.
     *
     * @param fileName  e.g. "testdata.json"
     * @param key       e.g. "users"
     * @param itemClass The class for each list item
     */
    public static <T> List<T> readJsonArrayFromFile(String fileName, String key, Class<T> itemClass) {
        try {
            InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (stream == null) {
                throw new RuntimeException("JSON file not found: " + fileName);
            }
            JsonNode root = objectMapper.readTree(stream);
            JsonNode arrayNode = root.get(key);
            if (arrayNode == null || !arrayNode.isArray()) {
                throw new RuntimeException("Key [" + key + "] not found or is not an array in " + fileName);
            }
            List<T> result = objectMapper.convertValue(arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, itemClass));
            log.debug("Read {} items from [{}] → key [{}]", result.size(), fileName, key);
            return result;
        } catch (Exception e) {
            log.error("Failed to read JSON array from file [{}] key [{}]", fileName, key, e);
            throw new RuntimeException("Error reading JSON array", e);
        }
    }

    // =========================================================
    // SERIALIZATION / DESERIALIZATION METHODS
    // =========================================================

    /**
     * Converts a POJO to a JSON string (pretty-printed).
     */
    public static String toJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            log.debug("Serialized object to JSON: {}", json);
            return json;
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Converts a JSON string to a POJO of the specified class.
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            T result = objectMapper.readValue(json, valueType);
            log.debug("Deserialized JSON to [{}]", valueType.getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to [{}]", valueType.getSimpleName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Converts a REST Assured Response body to a POJO.
     */
    public static <T> T fromResponse(Response response, Class<T> valueType) {
        return fromJson(response.getBody().asString(), valueType);
    }

    /**
     * Extracts a specific field value (as String) from a REST Assured Response.
     *
     * @param response  REST Assured Response
     * @param jsonPath  e.g. "data.id" or "token"
     */
    public static String extractField(Response response, String jsonPath) {
        String value = response.jsonPath().getString(jsonPath);
        log.debug("Extracted [{}] from response: [{}]", jsonPath, value);
        return value;
    }

    /**
     * Extracts an integer field from a REST Assured Response.
     */
    public static int extractIntField(Response response, String jsonPath) {
        int value = response.jsonPath().getInt(jsonPath);
        log.debug("Extracted int [{}] from response: [{}]", jsonPath, value);
        return value;
    }

    /**
     * Converts any object (e.g., a Map) into the target POJO class.
     * Useful when Jackson deserializes JSON into LinkedHashMap and you need a typed object.
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    /**
     * Returns the shared ObjectMapper instance.
     * Use when you need advanced Jackson features.
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
