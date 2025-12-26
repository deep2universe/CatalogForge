---
inclusion: fileMatch
fileMatchPattern: ['catForge-backend/**/*.java', 'catForge-backend/**/*.gradle.kts', 'catForge-backend/**/*.yml']
---

# Backend Tech Stack

## Runtime & Build

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Records, pattern matching, virtual threads |
| Spring Boot | 3.4.1 | REST API, WebFlux reactive HTTP |
| Gradle | 8.x | Kotlin DSL build system |

## Dependencies

**Core:**
- `spring-boot-starter-web` - REST controllers
- `spring-boot-starter-webflux` - Reactive WebClient for Gemini API
- `spring-boot-starter-validation` - Jakarta Bean Validation

**JSON:**
- `jackson-databind` + `jackson-datatype-jsr310` - JSON with Java 8+ date/time support

**External:**
- Google Gemini API - LLM for layout generation and vision analysis
- Puppeteer (Node.js) - Headless Chrome PDF generation via `scripts/pdf-generator.js`

## Testing Stack

| Library | Version | Use Case |
|---------|---------|----------|
| JUnit 5 | 5.11.3 | Test framework |
| jqwik | 1.9.2 | Property-based testing |
| Mockito | 5.14.2 | Mocking |
| AssertJ | 3.26.3 | Fluent assertions |
| JSONAssert | 1.5.3 | JSON comparison |

## Commands

```bash
./gradlew bootRun           # Start application
./gradlew test              # All tests
./gradlew unitTest          # @Tag("unit") only
./gradlew integrationTest   # @Tag("integration") only
./gradlew propertyTest      # @Tag("property") only
./gradlew jacocoTestReport  # Coverage report
```

## Environment

**Required:** `GEMINI_API_KEY` - Google Gemini API key (set in `.env` or environment)

## Configuration Prefixes

All in `src/main/resources/application.yml`:

- `catalogforge.gemini.*` - API key, model selection, timeouts
- `catalogforge.layout.*` - Generation options, variant count
- `catalogforge.puppeteer.*` - PDF settings, script path
- `catalogforge.skills.*` - Skills directory, caching
- `catalogforge.images.*` - Upload limits, allowed types

## Code Patterns

**Use Java 21 features:**
- Records for DTOs and immutable data
- Pattern matching in switch expressions
- `var` for local type inference where clear

**Spring conventions:**
- Constructor injection (no `@Autowired` on fields)
- `@ConfigurationProperties` for typed config
- `ResponseEntity<T>` for controller responses

**Reactive HTTP:**
- Use `WebClient` for external API calls (Gemini)
- Chain with `.block()` only at service boundaries
