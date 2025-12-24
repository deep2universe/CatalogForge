# Design Document: CatalogForge Backend

## Overview

CatalogForge ist eine Spring Boot 3.4 REST API, die generative KI (Google Gemini) nutzt, um professionelle Produktkataloge und Flyer zu erstellen. Das System transformiert Text-Prompts oder Referenzbilder zusammen mit Produktdaten in druckfertige HTML/CSS-Layouts, die anschließend in PDFs konvertiert werden können.

Die Kernarchitektur basiert auf drei Säulen:
1. **Skills-System**: Markdown-basierte Prompt-Bausteine, die dem LLM präzise Design-Constraints mitgeben
2. **Agent-Orchestrierung**: Flexible Pipeline-Strategien (Linear, Iterativ, Parallel) für verschiedene Generierungsanforderungen
3. **LLM-Logging**: Vollständige Protokollierung aller KI-Interaktionen für Debugging und Optimierung

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              REST Controllers                                │
│  ProductController │ LayoutController │ SkillsController │ PdfController    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                Services                                      │
│  ProductService │ LayoutGenerationService │ SkillsService │ ImageService    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
            ┌───────────┐   ┌─────────────┐   ┌──────────┐
            │   Agent   │   │   Skills    │   │  Gemini  │
            │Orchestrator│   │  Assembler  │   │  Client  │
            └───────────┘   └─────────────┘   └──────────┘
                    │               │               │
                    ▼               ▼               ▼
            ┌───────────┐   ┌─────────────┐   ┌──────────┐
            │ Pipelines │   │ Skill Files │   │ Gemini   │
            │Lin/Iter/Par│   │   (*.md)    │   │   API    │
            └───────────┘   └─────────────┘   └──────────┘
                    │
                    ▼
            ┌───────────────────┐
            │   LLM Logger      │
            │  (JSONL Files)    │
            └───────────────────┘
```

## Components and Interfaces

### Controller Layer

#### ProductController
```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    GET  /                    → ProductListResponse (paginated, filterable)
    GET  /{id}                → Product
    GET  /categories          → List<String>
    GET  /series              → List<String>
    GET  /search?q={query}    → ProductListResponse
}
```

#### LayoutController
```java
@RestController
@RequestMapping("/api/v1/layouts")
public class LayoutController {
    POST /generate/text       → LayoutResponse
    POST /generate/image      → LayoutResponse (with imageAnalysis)
    GET  /{id}                → Layout
    PUT  /{id}                → Layout
    DELETE /{id}              → void (204)
    GET  /{id}/variants       → List<LayoutVariant>
}
```

#### ImageController
```java
@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    POST /upload              → ImageUploadResponse (imageId, imageUrl)
}
```

#### SkillsController
```java
@RestController
@RequestMapping("/api/v1/skills")
public class SkillsController {
    GET  /                    → List<SkillMetadata>
    GET  /{category}          → List<SkillMetadata>
    GET  /prompts/examples    → List<ExamplePrompt>
}
```

#### PdfController
```java
@RestController
@RequestMapping("/api/v1/pdf")
public class PdfController {
    POST /generate            → PdfResponse (pdfId, downloadUrl)
    GET  /{id}/download       → byte[] (PDF file)
}
```

### Service Layer

#### ProductService
- Lädt `products.json` beim Startup in Memory-Cache
- Bietet Filterung nach category, series
- Case-insensitive Volltextsuche über name und description
- Keine Datenbank-Abhängigkeit

#### LayoutGenerationService
- Koordiniert Layout-Generierung via AgentOrchestrator
- Verwaltet Layout-Lifecycle (CRUD) in Memory-Store
- Wählt Pipeline-Strategie basierend auf Request-Optionen

#### SkillsService
- Delegiert an SkillLoader und SkillAssembler
- Cached geladene Skills
- Liefert Skill-Metadaten für API

#### ImageService
- Verwaltet temporäre Bild-Uploads
- Validiert Dateiformate (JPEG, PNG, WebP) und Größe (max 10MB)
- Generiert temporäre URLs (24h gültig)
- Cleanup-Job für abgelaufene Bilder

### Agent Framework

#### AgentOrchestrator
```java
public class AgentOrchestrator {
    public AgentResult execute(AgentContext context) {
        PipelineStrategy strategy = selectStrategy(context);
        Pipeline pipeline = strategy.createPipeline();
        return pipeline.run(context);
    }
    
    private PipelineStrategy selectStrategy(AgentContext context) {
        if (context.options().variantCount() > 1) {
            return new MultiVariantStrategy();
        }
        if (context.options().complexStrategy()) {
            return new ComplexLayoutStrategy();
        }
        return new SimpleLayoutStrategy();
    }
}
```

#### Pipeline Interface
```java
public interface Pipeline {
    AgentResult run(AgentContext context);
}
```

#### LinearPipeline
- Führt Steps sequentiell aus: PromptAssembly → LayoutGeneration
- 1 LLM-Call
- Für Standard-Layouts mit klaren Vorgaben

#### IterativePipeline
- PromptAssembly → LayoutGeneration → Validation → (Correction → Validation)*
- 1-4 LLM-Calls (mit Retries)
- FallbackStep bei Überschreitung von maxRetries
- Für komplexe Layouts mit strenger Qualitätskontrolle

#### ParallelPipeline
- PromptAssembly → N × LayoutGeneration (parallel) → Aggregation
- N LLM-Calls
- Für Multi-Varianten-Generierung

### Agent Steps

```java
public interface AgentStep {
    AgentContext execute(AgentContext context);
}
```

| Step | Verantwortung |
|------|---------------|
| PromptAssemblyStep | Assembliert Skills, injiziert Produktdaten und Bildanalyse |
| ImageAnalysisStep | Ruft Vision_Analyzer für Farbpalette, Mood, LayoutHints |
| LayoutGenerationStep | Ruft Gemini API für HTML/CSS-Generierung |
| ValidationStep | Validiert generiertes HTML/CSS |
| CorrectionStep | Sendet Korrektur-Prompt bei Validierungsfehlern |
| FallbackStep | Liefert Fallback-Layout bei wiederholtem Fehlschlag |

### AgentContext (Immutable Record)
```java
public record AgentContext(
    String pipelineId,
    String userPrompt,
    List<Product> products,
    LayoutOptions options,
    String imageUrl,
    String assembledSystemPrompt,
    String assembledUserPrompt,
    ColorPalette colorPalette,
    MoodAnalysis mood,
    LayoutHints layoutHints,
    boolean placeholderMode,
    String generatedHtml,
    String generatedCss,
    List<ValidationError> errors
) {}
```

### Skills System

#### Skill Record
```java
public record Skill(
    String name,
    String category,
    List<String> dependencies,
    int priority,
    String content
) {}
```

#### SkillLoader
- Scannt `resources/skills/` rekursiv nach `.md` Dateien
- Extrahiert Name aus Dateiname (PRODUCT_PAGE.md → PRODUCT_PAGE)
- Extrahiert Category aus Verzeichnispfad (skills/core/X.md → core)
- Parst Metadata-Header (dependencies, priority)

#### SkillAssembler
```java
public class SkillAssembler {
    public String assemble(LayoutOptions options, ImageAnalysisResult imageAnalysis) {
        Set<String> requiredSkills = determineRequiredSkills(options, imageAnalysis);
        Set<String> allSkills = resolveDependencies(requiredSkills);
        List<Skill> sorted = sortByPriority(allSkills);
        return mergeContents(sorted);
    }
}
```

Assembly-Regeln:
1. MASTER_SKILL immer zuerst
2. Dependencies rekursiv auflösen
3. Nach Priority sortieren (höher = früher)
4. Contents zusammenführen

#### Skill-Kategorien
| Kategorie | Beispiele |
|-----------|-----------|
| core | LAYOUT_PRINCIPLES, TYPOGRAPHY, COLOR_THEORY, GRID_SYSTEMS |
| marketing | CATALOG_DESIGN, AUTOMOTIVE_B2B, VISUAL_HIERARCHY |
| page-types | PRODUCT_PAGE, OVERVIEW_PAGE, COVER_PAGE, FLYER_PAGE |
| styles | STYLE_MODERN, STYLE_TECHNICAL, STYLE_PREMIUM, STYLE_ECO |
| formats | FORMAT_A4, FORMAT_A5, FORMAT_DL, FORMAT_SQUARE |
| image-analysis | COLOR_EXTRACTION, MOOD_ANALYSIS, LAYOUT_INSPIRATION |

### Gemini Integration

#### GeminiClient
```java
public class GeminiClient {
    public GeminiResponse generate(GeminiRequest request);
}
```

- HTTP POST via Spring WebClient
- Header: x-goog-api-key
- Timeout: 60s (konfigurierbar)
- Modelle:
  - gemini-2.5-flash: Standard-Layouts
  - gemini-2.5-pro: Komplexe Layouts
  - gemini-2.5-pro-vision: Bildanalyse

#### GeminiRequest
```java
public record GeminiRequest(
    String model,
    String systemInstruction,
    String userPrompt,
    String imageUrl,  // Optional, für Vision
    Class<?> responseSchema
) {}
```

#### GeminiVisionAnalyzer
```java
public class GeminiVisionAnalyzer {
    public ImageAnalysisResult analyze(String imageUrl, AnalysisOptions options);
}
```

Extrahiert:
- ColorPalette: primary, secondary, accent, neutralLight, neutralDark
- MoodAnalysis: type, confidence, keywords
- LayoutHints: gridType, density, focusArea, suggestedColumns

### LLM Logging System

#### LlmInteractionLogger
```java
public class LlmInteractionLogger {
    public String logRequest(String pipelineId, String stepName, GeminiRequest request);
    public void logResponse(String requestId, GeminiResponse response, long durationMs);
    public void logError(String requestId, Exception error);
}
```

#### LlmLogEntry
```java
public record LlmLogEntry(
    String timestamp,
    String requestId,
    String pipelineId,
    String stepName,
    Direction direction,  // REQUEST | RESPONSE
    String model,
    // REQUEST fields
    String systemPrompt,
    String userPrompt,
    String imageUrl,
    int inputTokensEstimate,
    // RESPONSE fields
    long durationMs,
    Status status,  // SUCCESS | ERROR
    int outputTokens,
    Object response,
    String errorType,
    String errorMessage
) {}
```

#### LlmLogWriter
- Schreibt in tägliche JSONL-Dateien: `logs/llm/{date}_llm.jsonl`
- Eine JSON-Zeile pro Entry (keine eingebetteten Newlines)
- Append-Modus (kein Überschreiben)
- Neue Datei bei Datumswechsel

### PDF Generation

#### PuppeteerBridge
```java
public class PuppeteerBridge {
    public PdfResult generate(String html, String css, PrintPreset preset);
}
```

Kommunikation via stdin/stdout JSON mit Node.js Puppeteer-Prozess.

#### PrintPresets
| Preset | DPI | Bleed | Schnittmarken |
|--------|-----|-------|---------------|
| screen | 72 | - | Nein |
| print-standard | 150 | - | Nein |
| print-professional | 300 | 3mm | Ja |
| print-premium | 300 | 5mm | Ja + Passkreuze |

### Image Upload Service

#### ImageService
```java
public class ImageService {
    public ImageUploadResult upload(MultipartFile file);
    public Optional<Path> getImage(String imageId);
    public void cleanup();  // Scheduled, löscht Bilder älter als 24h
}
```

- Speichert in temporärem Verzeichnis
- Generiert UUID als imageId
- Erstellt interne URL: `/api/v1/images/{imageId}`
- Validiert: JPEG, PNG, WebP, max 10MB

## Data Models

### Product
```java
public record Product(
    Long id,
    String name,
    String description,
    String category,
    String series,
    TechnicalData specs,
    String imageUrl
) {}

public record TechnicalData(
    Map<String, String> specifications
) {}
```

### Layout
```java
public record Layout(
    String id,
    String status,
    Instant generatedAt,
    PageFormat pageFormat,
    ImageAnalysisResult imageAnalysis,
    List<LayoutVariant> variants,
    LayoutMetadata metadata
) {}

public record LayoutVariant(
    String id,
    String html,
    String css
) {}

public record LayoutMetadata(
    List<String> skillsUsed,
    long generationTimeMs,
    int llmCallCount
) {}
```

### Image Analysis
```java
public record ColorPalette(
    String primary,
    String secondary,
    String accent,
    String neutralLight,
    String neutralDark
) {}

public record MoodAnalysis(
    String type,
    double confidence,
    List<String> keywords
) {}

public record LayoutHints(
    String gridType,
    String density,
    String focusArea,
    int suggestedColumns
) {}

public record ImageAnalysisResult(
    ColorPalette colorPalette,
    MoodAnalysis mood,
    LayoutHints layoutHints
) {}
```

### Request/Response DTOs
```java
public record TextToLayoutRequest(
    String prompt,
    List<Long> productIds,
    LayoutOptions options
) {}

public record ImageToLayoutRequest(
    String imageUrl,
    List<Long> productIds,
    ImageLayoutOptions options
) {}

public record LayoutOptions(
    String pageFormat,
    String style,
    int variantCount,
    boolean includeSpecs,
    boolean complexStrategy
) {}

public record ImageLayoutOptions(
    String pageFormat,
    boolean extractColors,
    boolean analyzeMood,
    boolean analyzeLayout
) {}

public record LayoutResponse(
    String id,
    String status,
    Instant generatedAt,
    PageFormat pageFormat,
    ImageAnalysisResult imageAnalysis,
    List<LayoutVariant> variants,
    LayoutMetadata metadata,
    boolean placeholdersUsed
) {}

public record ImageUploadResponse(
    String imageId,
    String imageUrl,
    Instant expiresAt
) {}

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Product Data Integrity
*For any* products.json file, loading and then retrieving all products should return the same set of products with all fields intact (id, name, description, category, series, specs, imageUrl).
**Validates: Requirements 1.1, 1.3**

### Property 2: Product Filtering Correctness
*For any* filter combination (category, series), all returned products must satisfy all specified filter criteria (AND-semantics).
**Validates: Requirements 1.2, 1.4**

### Property 3: Search Result Relevance
*For any* search query, all returned products must contain the query string (case-insensitive) in either name or description field.
**Validates: Requirements 1.5**

### Property 4: Categories and Series Uniqueness
*For any* product dataset, the returned categories list and series list must contain no duplicates and be sorted alphabetically.
**Validates: Requirements 1.6, 1.7**

### Property 5: Layout Response Completeness
*For any* successful layout generation, the response must contain: id (valid UUID), status, generatedAt, pageFormat, variants (non-empty array with html and css), and metadata (skillsUsed, generationTimeMs).
**Validates: Requirements 2.1, 2.7**

### Property 6: Pipeline Selection Logic
*For any* layout request:
- variantCount > 1 → ParallelPipeline selected
- complexStrategy = true → IterativePipeline selected
- otherwise → LinearPipeline selected
**Validates: Requirements 2.5, 2.6, 7.1, 7.2, 7.3**

### Property 7: Skill Assembly Ordering
*For any* skill assembly:
- MASTER_SKILL appears first in the assembled prompt
- All dependencies are included before dependents
- Skills are ordered by priority (descending) within dependency constraints
**Validates: Requirements 6.1, 6.2, 6.3, 6.4**

### Property 8: Style and Format Skill Inclusion
*For any* layout request with style option, the assembled prompt must contain the corresponding STYLE_* skill content. *For any* request with pageFormat option, the assembled prompt must contain the corresponding FORMAT_* skill content.
**Validates: Requirements 2.3, 2.4, 6.5, 6.6**

### Property 9: Image Analysis Extraction
*For any* reachable image URL with analysis options enabled:
- extractColors=true → ColorPalette contains primary, secondary, accent, neutralLight, neutralDark
- analyzeMood=true → MoodAnalysis contains type, confidence, keywords
- analyzeLayout=true → LayoutHints contains gridType, density, focusArea, suggestedColumns
**Validates: Requirements 3.2, 3.4, 3.5, 3.6**

### Property 10: Image Upload Round-Trip
*For any* valid image file (JPEG/PNG/WebP, ≤10MB), uploading and then using the returned imageUrl for image-to-layout generation should successfully analyze the image.
**Validates: Requirements 4.1, 4.2, 4.5, 4.6**

### Property 11: Gemini Request Structure
*For any* Gemini API call, the request must contain: valid model name, systemInstruction, userPrompt, and x-goog-api-key header.
**Validates: Requirements 8.1, 8.5**

### Property 12: Gemini Model Selection
*For any* layout generation: model = gemini-2.5-flash.
*For any* image analysis: model = gemini-2.5-pro-vision.
*For any* complex layout (complexStrategy=true): model = gemini-2.5-pro.
**Validates: Requirements 8.2, 8.3, 8.4**

### Property 13: Layout Lifecycle Round-Trip
*For any* generated layout:
- GET /{id} returns the same layout
- PUT /{id} with new content, then GET /{id} returns updated content
- DELETE /{id}, then GET /{id} returns 404
**Validates: Requirements 9.1, 9.2, 9.4, 9.5**

### Property 14: LLM Log Correlation
*For any* LLM call, there exists exactly one REQUEST entry and one RESPONSE entry with matching requestId, and the RESPONSE entry has durationMs > 0.
**Validates: Requirements 10.1, 10.2, 10.3**

### Property 15: Log File Format
*For any* log file:
- Filename matches pattern {date}_llm.jsonl
- Each line is valid JSON
- Multiple writes append (file grows, never shrinks)
**Validates: Requirements 10.4, 10.5, 10.6, 10.7**

### Property 16: HTML Sanitization
*For any* HTML input:
- Output contains no `<script>` tags
- Output contains no event handler attributes (onclick, onload, onerror, etc.)
- Structural elements (div, span, p, h1-h6, img, table) are preserved
- CSS classes are preserved
**Validates: Requirements 12.1, 12.2, 12.3**

### Property 17: CSS Validation
*For any* CSS input:
- Unbalanced brackets are detected as invalid
- Print units (mm, cm, pt, in) are accepted as valid
**Validates: Requirements 12.4, 12.5**

### Property 18: Color Validation and Contrast
*For any* string:
- Valid hex codes (#RGB, #RRGGBB) return isValid=true
- Invalid formats return isValid=false
*For any* two valid colors, WCAG contrast ratio is calculated correctly (verified against known values: #000/#FFF = 21.0).
**Validates: Requirements 12.6, 12.7**

### Property 19: Error Response Structure
*For any* error response, the body must contain: timestamp, status, error, message, and path fields.
**Validates: Requirements 14.6**

## Error Handling

### Exception Hierarchy
```
CatalogForgeException (abstract)
├── LayoutGenerationException
├── PdfGenerationException
├── SkillLoadException
├── ImageAnalysisException
├── ImageUrlNotReachableException
├── ImageUploadException
└── ResourceNotFoundException
```

### GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...)  // 400
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...)    // 404
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(...)    // 413
    
    @ExceptionHandler(ImageAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleGeminiError(...) // 502
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(...)     // 500
}
```

### Error Codes
| HTTP Status | Scenario |
|-------------|----------|
| 400 | Validation error (empty prompt, invalid format) |
| 404 | Resource not found (product, layout, skill category) |
| 413 | File too large (>10MB) |
| 502 | Gemini API error |
| 500 | Internal server error |

## Testing Strategy

### Test Types

#### Unit Tests (@Tag("unit"))
- Isolierte Tests ohne Spring Context
- Alle Dependencies gemockt
- Laufzeit < 30 Sekunden
- Fokus: Business-Logik, Algorithmen

#### Integration Tests (@Tag("integration"))
- Mit Spring Context
- Nur externe APIs (Gemini) gemockt
- Echte products.json und Skills
- Laufzeit < 5 Minuten
- Fokus: Komponenten-Zusammenspiel

### Property-Based Testing
- Framework: **jqwik** (JUnit 5 kompatibel)
- Minimum 100 Iterationen pro Property
- Jeder Test referenziert Design-Property
- Tag-Format: `@Tag("property")` + `@Label("Property N: ...")`

### Test Data Strategy
- Echte `products.json` in Tests verwenden
- Echte Skills in Tests verwenden
- Gemini-Responses als JSON-Fixtures in `src/test/resources/fixtures/gemini-responses/`

### Coverage Goals
| Package | Target |
|---------|--------|
| agent/ | 80%+ |
| gemini/ | 80%+ |
| skill/ | 75%+ |
| service/ | 70%+ |
| controller/ | 70%+ |
| logging/ | 70%+ |
| util/ | 60%+ |

**Gesamtziel: 70% Line Coverage**
