package com.api.framework.utils;

import com.github.javafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.UUID;

/**
 * =========================================================
 * RandomDataGenerator.java
 * =========================================================
 * Generates realistic random test data using Java Faker.
 * Useful for creating unique payloads in each test run
 * to avoid data conflicts.
 *
 * Usage:
 *   String name  = RandomDataGenerator.getFullName();
 *   String email = RandomDataGenerator.getEmail();
 *   String job   = RandomDataGenerator.getJobTitle();
 * =========================================================
 */
public class RandomDataGenerator {

    private static final Logger log = LogManager.getLogger(RandomDataGenerator.class);

    // Faker instance - using English locale for consistent data
    private static final Faker faker = new Faker(Locale.ENGLISH);

    // Private constructor - static utility class
    private RandomDataGenerator() {}

    // =========================================================
    // PERSON DATA
    // =========================================================

    /** Returns a random full name e.g. "John Doe" */
    public static String getFullName() {
        String name = faker.name().fullName();
        log.debug("Generated name: {}", name);
        return name;
    }

    /** Returns a random first name */
    public static String getFirstName() {
        return faker.name().firstName();
    }

    /** Returns a random last name */
    public static String getLastName() {
        return faker.name().lastName();
    }

    /**
     * Returns a unique email address.
     * Uses UUID prefix to guarantee uniqueness across test runs.
     * e.g. "a1b2c3d4@test-domain.com"
     */
    public static String getEmail() {
        String email = UUID.randomUUID().toString().substring(0, 8)
                + "@" + faker.internet().domainName();
        log.debug("Generated email: {}", email);
        return email;
    }

    /** Returns a random job title e.g. "Senior Software Engineer" */
    public static String getJobTitle() {
        String job = faker.job().title();
        log.debug("Generated job: {}", job);
        return job;
    }

    /** Returns a random phone number */
    public static String getPhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    // =========================================================
    // ADDRESS DATA
    // =========================================================

    /** Returns a random city name */
    public static String getCity() {
        return faker.address().city();
    }

    /** Returns a random country */
    public static String getCountry() {
        return faker.address().country();
    }

    // =========================================================
    // NUMERIC DATA
    // =========================================================

    /** Returns a random integer between min and max (inclusive) */
    public static int getRandomInt(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    /** Returns a random positive user ID (1-100) */
    public static int getRandomUserId() {
        return faker.number().numberBetween(1, 100);
    }

    // =========================================================
    // STRING DATA
    // =========================================================

    /** Returns a random alphanumeric string of given length */
    public static String getRandomAlphanumeric(int length) {
        return faker.lorem().characters(length, true, true);
    }

    /** Returns a random UUID string */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /** Returns a random company name */
    public static String getCompanyName() {
        return faker.company().name();
    }
}
