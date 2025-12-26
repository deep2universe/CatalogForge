---
inclusion: always
---

# Project Structure

## Backend (`catForge-backend/`)

```
src/main/java/com/catalogforge/
├── agent/              # Agent framework for layout generation pipelines
│   ├── steps/          # Pipeline steps: ImageAnalysis, LayoutGeneration, Validation
│   └── strategies/     # Pipeline strategies: Simple, Complex, MultiVariant
├── config/             # Spring @Configuration classes
│   └── properties/     # @ConfigurationProperties (Gemini, Layout, Puppeteer, etc.)
├── controller/         # REST controllers (@RestController, /api/v1/*)
├── exception/          # Custom exceptions + GlobalExceptionHandler
├── gemini/             # Gemini API integration (client, request/response, vision)
├── logging/            # LLM interaction logging (JSONL format)
├── model/              # Domain models as Java records
│   ├── request/        # Request DTOs
│   └── response/       # Response DTOs
├── pdf/                # PDF generation via Puppeteer bridge
├── service/            # Business logic services
├── skill/              # Skills system (markdown-based prompt engineering)
└── util/               # Utilities: CssValidator, HtmlSanitizer, ColorUtils

src/main/resources/
├── css/                # CSS templates (components/, print/)
├── data/products.json  # Product catalog data
├── prompts/            # Example prompts
└── skills/             # Skill markdown files
    ├── core/           # MASTER_SKILL, TYPOGRAPHY, COLOR_THEORY, GRID_SYSTEMS
    ├── formats/        # FORMAT_A4, FORMAT_A5, FORMAT_DL, FORMAT_A6, FORMAT_SQUARE
    └── styles/         # STYLE_MODERN, STYLE_TECHNICAL, STYLE_PREMIUM, etc.

src/test/
├── java/               # Test classes (mirrors main structure)
└── resources/fixtures/ # Test fixtures (Gemini response mocks)

scripts/                # Node.js scripts (pdf-generator.js)
```

## Code Conventions

### Records for Immutable Data
Use Java records for DTOs, domain models, and context objects:
```java
public record Layout(String id, String status, List<LayoutVariant> variants) {
    public Layout { variants = variants != null ? List.copyOf(variants) : List.of(); }
}
```

### Immutable Context Pattern
Use `withX()` methods that return new instances for state changes:
```java
public AgentContext withGeneratedLayout(Layout layout) {
    return new AgentContext(/* copy all fields, replace layout */);
}
```

### Factory Methods
Use static factory methods for context creation:
```java
AgentContext.forTextGeneration(products, options, prompt)
AgentContext.forImageGeneration(products, options, prompt, imageBase64, mimeType)
```

### Constructor Injection
Always use constructor injection (no `@Autowired` on fields):
```java
public LayoutController(LayoutGenerationService layoutService) {
    this.layoutService = layoutService;
}
```

### Logging
Use SLF4J with appropriate levels:
- `log.info()` for operations (API calls, generation requests)
- `log.debug()` for details (lookups, internal state)

### Javadoc
Document public methods with Javadoc including endpoint paths for controllers:
```java
/**
 * Generates a layout from text prompt.
 * POST /api/v1/layouts/generate/text
 */
```

## Testing Conventions

### Test Tags
Use JUnit 5 tags for test categorization:
- `@Tag("unit")` - Unit tests
- `@Tag("integration")` - Integration tests  
- `@Tag("property")` - Property-based tests (jqwik)

### Property-Based Tests
Use jqwik for validation logic with documented properties:
```java
@Property(tries = 100)
@Label("Property 18: Color Validation - valid 6-digit hex codes")
void valid6DigitHexCodesAccepted(@ForAll @CharRange(from = '0', to = '9') char c1, ...) {
    assertThat(ColorUtils.isValidHexColor(color)).isTrue();
}
```

### Assertions
Use AssertJ for fluent assertions:
```java
assertThat(products).extracting(Product::id).doesNotContainNull().doesNotHaveDuplicates();
```

### Test Structure
Use nested classes with `@DisplayName` for organization:
```java
@Nested
@DisplayName("Product Loading Tests")
class ProductLoadingTests { ... }
```

## API Conventions

- Base path: `/api/v1/`
- Use `@Valid` for request validation
- Return `ResponseEntity` with appropriate status codes
- Use `LayoutResponse.from(layout)` pattern for DTO conversion
