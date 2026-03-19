package com.api.framework.utils;

import com.api.framework.constants.ApiConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * =========================================================
 * ConfigReader.java
 * =========================================================
 * Singleton utility to load configuration from properties files.
 *
 * Supports multiple environments:
 *   - QA:   config.properties (default)
 *   - UAT:  config-uat.properties
 *   - PROD: config-prod.properties
 *
 * Environment selection via Maven system property:
 *   mvn test -Denv=uat
 *
 * Usage:
 *   ConfigReader.get("base.url")
 * =========================================================
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);

    // Singleton instance
    private static ConfigReader instance;

    // Holds the loaded properties
    private final Properties properties;

    // Private constructor - loads properties on creation
    private ConfigReader() {
        properties = new Properties();
        String env = System.getProperty("env", ApiConstants.ENV_QA).toLowerCase();
        String fileName = resolveConfigFileName(env);

        log.info("Loading configuration for environment: [{}] from file: [{}]", env, fileName);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file not found: " + fileName);
            }
            properties.load(inputStream);
            log.debug("Configuration loaded successfully. Total properties: {}", properties.size());
        } catch (IOException e) {
            log.error("Failed to load configuration file: {}", fileName, e);
            throw new RuntimeException("Could not load config file: " + fileName, e);
        }
    }

    /**
     * Determines config file name based on environment.
     * QA uses default 'config.properties'; others get 'config-{env}.properties'
     */
    private String resolveConfigFileName(String env) {
        if (env.equals(ApiConstants.ENV_QA)) {
            return "config.properties";
        }
        return "config-" + env + ".properties";
    }

    /**
     * Thread-safe lazy singleton initialization.
     * Double-checked locking pattern.
     */
    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    /**
     * Gets a property value by key.
     * Throws RuntimeException if key is not found.
     */
    public static String get(String key) {
        String value = getInstance().properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            log.error("Property not found: [{}]", key);
            throw new RuntimeException("Missing property in config: " + key);
        }
        log.debug("Config read → [{}] = [{}]", key, value);
        return value.trim();
    }

    /**
     * Gets a property value with a fallback default.
     * Does NOT throw if key is missing.
     */
    public static String getOrDefault(String key, String defaultValue) {
        String value = getInstance().properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            log.warn("Property [{}] not found. Using default: [{}]", key, defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Gets a property value as integer.
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Returns the base URL from config.
     */
    public static String getBaseUrl() {
        return get(ApiConstants.PROP_BASE_URL);
    }

    /**
     * Returns the /users endpoint path.
     */
    public static String getUsersEndpoint() {
        return get(ApiConstants.PROP_ENDPOINT_USERS);
    }

    /**
     * Returns the /login (auth) endpoint path.
     */
    public static String getAuthEndpoint() {
        return get(ApiConstants.PROP_ENDPOINT_AUTH);
    }
}
