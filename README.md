# GitHub User Project

Spring Boot service that fetches a GitHub user’s profile and repositories from the GitHub public API and returns a consolidated response. Results are cached to reduce repeat calls and improve performance.

---

## Tech stack
- Java 21
- Spring Boot 3 (Web, Cache)
- RestTemplate (blocking HTTP client)
- Caffeine (cache provider)
- Lombok
- Gradle

---

## Requirements
- JDK 21+
- Internet access (calls GitHub public API)
- cURL/Postman/HTTPie for manual testing (optional)

---

## Configuration
Application settings are in `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: githubUserProject

github:
  api:
    user-base-url: "https://api.github.com/users/"
    repo-base-url: "https://api.github.com/repos/"
```

Notes:
- Requests are built by appending `{username}` (and `/repos`) to `github.api.user-base-url`.
- `repo-base-url` is used to format repository URLs in the response.
- Caffeine cache name: `userCache` with a 10‑minute TTL (configured in `GithubUserApplication#cacheManager`).

---

## Build and run
Using the Gradle Wrapper (recommended):

```bash
./gradlew clean build
./gradlew bootRun
```

The app starts on: `http://localhost:8080`.

Run tests only:

```bash
./gradlew test
```

---

## API
Retrieve consolidated GitHub user info (basic profile + repositories):

- Method: `GET`
- Path: `/users/{username}`

Example:

```bash
curl -i http://localhost:8080/users/octocat
```

Successful response (200) includes selected user fields and a list of repositories with `name` and fully qualified `url`.

---

## How to verify caching works

1) Start the app and enable default logging.
2) Call the endpoint twice within 10 minutes using the same username:

```bash
curl -s http://localhost:8080/users/octocat > /dev/null
curl -s http://localhost:8080/users/octocat > /dev/null
```

3) Check the application logs. The service logs when it makes outbound calls to GitHub:
- First request: look for a line like `Making service call to Github user data for: octocat`.
- Second request (within cache window): this log should NOT appear, which indicates the result came from cache.

If you wait longer than the cache expiry (default 10 minutes), the next call will re‑fetch and you’ll see the log again.

---

## Tests
Run all tests:

```bash
./gradlew test
```

Tests are under `src/test/java`. They cover controller and service logic, including error handling scenarios. The project uses JUnit (Jupiter) and Mockito via Spring Boot’s test starter.

---

## Development notes and call‑outs

- Caching mechanism: Caffeine
  - Service responses are cached for 10 minutes in `userCache`.

- Unit Test: Junit, Juniper, Mockito (part of Java Test starter)
  - JUnit Jupiter and Mockito are available via `spring-boot-starter-test`.

- REST Template for making REST calls
  - `RestTemplate` is used to call GitHub’s user and repo endpoints.

- Log 4 J for logging details; use this to test that cache is working as implemented
  - Verify caching by checking that only the first call logs the outbound fetch for a given username.

- JSON formatting
  - To setup order of return
  - To define variable names coming in from API call

- Using Java objects to load data and reprint them as needed
  - API responses are mapped into Java objects and transformed into a consolidated DTO (`UserResponse`).

- Error handling:
  - If main GitHub URL details are empty, the call fails.
  - If repo GitHub URL details are empty, a warning is logged, but a 200 response still returns other details.
  - If any service error occurs, the service throws an error with details (mapped to appropriate HTTP status).
  - If any runtime exception occurs, it is caught at the controller and a 500 service error is returned.

- The services are not set to run parallelly
  - This is a known performance improvement to consider (e.g., parallelize user and repo fetches).

- The app is not configured to timeout requests
  - This is a known performance improvement to consider (e.g., use a timeout for outbound calls).

---

## Project layout

```
src/main/java/com/assessment/githubUser/
  GithubUserApplication.java        # Boot app, RestTemplate bean, Caffeine cache manager
  controller/UserController.java    # /users/{username}
  service/GithubUserService.java    # Fetch user + repos, assemble response, cache
  model/...                         # DTOs and REST object mappings
src/main/resources/application.yaml # Base URLs and app name
```

## Troubleshooting
- 404 Not Found: Returned when the GitHub username does not exist.
- 500 Internal Server Error: Unexpected runtime exceptions are mapped to 500 at the controller level.
- GitHub rate limiting: Public API enforces rate limits; rely on caching and avoid unnecessary repeat calls.