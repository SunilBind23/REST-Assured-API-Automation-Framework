# REST Assured API Automation Framework

> 🚀 Industry-level API test automation framework using REST Assured + TestNG + Java

---

## 📁 Project Structure

```
restassured-api-framework/
├── .github/workflows/
│   └── api-tests.yml                    # CI/CD pipeline
├── src/
│   ├── main/java/com/api/framework/
│   │   ├── base/
│   │   │   └── BaseApi.java             # REST Assured core setup
│   │   ├── constants/
│   │   │   └── ApiConstants.java        # All constants
│   │   ├── payloads/
│   │   │   ├── LoginRequest.java        # Request POJOs
│   │   │   ├── LoginResponse.java
│   │   │   ├── CreateUserRequest.java
│   │   │   ├── CreateUserResponse.java
│   │   │   ├── GetUserResponse.java
│   │   │   ├── UserData.java
│   │   │   ├── UpdateUserRequest.java
│   │   │   └── UpdateUserResponse.java
│   │   ├── services/
│   │   │   ├── AuthService.java         # Authentication API methods
│   │   │   └── UserService.java         # User CRUD API methods
│   │   └── utils/
│   │       ├── ConfigReader.java        # Environment config reader
│   │       ├── ExtentReportManager.java # Extent Reports lifecycle
│   │       ├── JsonUtils.java           # JSON read/write/parse
│   │       ├── LoggingFilter.java       # Custom REST Assured filter
│   │       ├── RandomDataGenerator.java # Fake test data (Faker)
│   │       ├── RetryAnalyzer.java       # Auto-retry failed tests
│   │       └── RetryListener.java       # Global retry injection
│   ├── main/resources/
│   │   ├── config.properties            # QA environment config
│   │   ├── config-uat.properties        # UAT environment config
│   │   └── log4j2.xml                   # Log4j2 configuration
│   └── test/
│       ├── java/com/api/framework/tests/
│       │   ├── BaseTest.java            # TestNG base with setup/teardown
│       │   ├── AuthTests.java           # Auth/login test cases
│       │   └── UserTests.java           # User CRUD test cases
│       └── resources/
│           ├── testng.xml               # TestNG suite definition
│           ├── testdata.json            # Test data (data-driven)
│           └── schemas/
│               ├── create-user-schema.json
│               └── get-user-schema.json
├── reports/                             # Generated HTML reports
├── logs/                                # Generated log files
└── pom.xml                              # Maven dependencies
```

---

## 🛠️ Tech Stack

| Layer            | Technology              |
|------------------|-------------------------|
| Language         | Java 11                 |
| API Testing      | REST Assured 5.3.x      |
| Test Framework   | TestNG 7.8.x            |
| Build Tool       | Maven                   |
| Reporting        | Extent Reports 5.x      |
| Logging          | Log4j2                  |
| JSON Parsing     | Jackson                 |
| Fake Data        | Java Faker              |
| Schema Validate  | JSON Schema Validator   |
| CI/CD            | GitHub Actions          |

---

## ⚡ Quick Start

### Prerequisites
- Java 11+
- Maven 3.6+
- Git

### Clone & Run
```bash
# Clone the repo
git clone https://github.com/your-org/restassured-api-framework.git
cd restassured-api-framework

# Run smoke tests (QA environment)
mvn test

# Run regression tests
mvn test -Dgroups=regression

# Run on UAT environment
mvn test -Denv=uat

# Run specific test class
mvn test -Dtest=UserTests

# Run specific test method
mvn test -Dtest=UserTests#testCreateUser
```

---

## 🌍 Environment Configuration

Switch environments using the `-Denv` Maven property:

```bash
mvn test -Denv=qa    # Default
mvn test -Denv=uat
mvn test -Denv=prod
```

Each environment has its own `config-{env}.properties` file in `src/main/resources/`.

---

## 🏗️ Key Design Concepts

### 1. Service Layer Pattern
Tests call service methods → services call REST Assured → returns Response.
Tests never use `given().when().then()` directly — keeps test classes clean.

```java
// ✅ Good: Clean test
Response response = userService.createUser("John", "Engineer");
Assert.assertEquals(response.getStatusCode(), 201);

// ❌ Bad: REST Assured in test class
given().body(...).when().post("/api/users").then().statusCode(201);
```

### 2. API Chaining
Response data from one API call is fed into the next:

```java
// Step 1: Login → get token
String token = authService.loginAndGetToken();

// Step 2: Use token in create user
Response createResp = userService.createUser("Jane", "QA");
String userId = createResp.jsonPath().getString("id");

// Step 3: Use userId to update
userService.updateUser(userId, updatePayload);
```

### 3. Data-Driven Testing
Test data lives in `testdata.json`. The `@DataProvider` reads it at runtime:

```java
@Test(dataProvider = "userDataProvider")
public void testCreateUserDataDriven(String name, String job, int expectedStatus) {
    Response response = userService.createUser(name, job);
    Assert.assertEquals(response.getStatusCode(), expectedStatus);
}
```

### 4. JSON Schema Validation
Validate response structure matches a defined JSON Schema:

```java
response.then().assertThat()
    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/create-user-schema.json"));
```

### 5. Parallel Execution
Configured in `testng.xml`:
```xml
<suite parallel="methods" thread-count="3">
```
Thread-safety ensured via `ThreadLocal` in `RetryAnalyzer` and `ExtentReportManager`.

---

## 📊 Test Reports

After running tests, open the HTML report:
```
reports/ExtentReport_yyyy-MM-dd_HH-mm-ss.html
```

The report includes:
- ✅ Pass / ❌ Fail / ⏭️ Skip summary
- Full HTTP request + response logs per test
- Environment and system info
- Retry attempts logged

---

## 🔁 CI/CD (GitHub Actions)

The pipeline runs automatically on:
- Push to `main` or `develop`
- Pull Request to `main`
- Manual trigger (choose env + groups)

Artifacts uploaded per run:
- Extent HTML Report (retained 30 days)
- Test Logs (retained 7 days)
- Surefire XML Reports

---

## 📋 Test Groups

| Group        | Description                          | Command                         |
|--------------|--------------------------------------|---------------------------------|
| `smoke`      | Critical happy-path tests            | `mvn test -Dgroups=smoke`       |
| `regression` | Full regression including negatives  | `mvn test -Dgroups=regression`  |
| `e2e`        | End-to-end API chaining tests        | `mvn test -Dgroups=e2e`         |
| `negative`   | Negative/error scenario tests        | `mvn test -Dgroups=negative`    |

---

## ➕ Adding a New Test

1. Create a service method in the appropriate `*Service.java`
2. Add POJO in `payloads/` if new request/response structure
3. Write test in the relevant `*Tests.java` or create a new test class
4. Add test data to `testdata.json` if data-driven
5. Register test class in `testng.xml`

---


