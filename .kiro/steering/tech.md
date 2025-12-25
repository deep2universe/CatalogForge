# Tech Stack

## Runtime & Framework

- **Java 21** (LTS) - Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.4** - REST API, WebFlux for reactive HTTP
- **Gradle 8.x** - Kotlin DSL build system

## Key Dependencies

- **Spring WebClient** - Reactive HTTP client for Gemini API calls
- **Jackson** - JSON processing with JSR-310 date support
- **Jakarta Validation** - Request validation
- **Puppeteer (Node.js)** - Headless Chrome for PDF generation
- **Google Gemini API** - LLM for layout generation and vision analysis

## Testing

- **JUnit 5** - Test framework
- **jqwik** - Property-based testing
- **Mockito** - Mocking
- **AssertJ** - Fluent assertions
- **JSONAssert** - JSON comparison
- **Spring Boot Test** - @WebMvcTest, @SpringBootTest

## Common Commands

```bash
# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run specific test types
./gradlew unitTest          # @Tag("unit")
./gradlew integrationTest   # @Tag("integration")
./gradlew propertyTest      # @Tag("property")

# Build
./gradlew build

# Generate test coverage report
./gradlew jacocoTestReport
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GEMINI_API_KEY` | Yes | Google Gemini API key |

## Configuration

Main config: `src/main/resources/application.yml`

Key prefixes:
- `catalogforge.gemini.*` - Gemini API settings
- `catalogforge.layout.*` - Layout generation options
- `catalogforge.puppeteer.*` - PDF generation settings
- `catalogforge.skills.*` - Skills system config
- `catalogforge.images.*` - Image handling settings
