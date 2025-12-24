# Requirements Document

## Introduction

CatalogForge ist eine KI-gestützte Plattform zur Erstellung professioneller Produktkataloge und Flyer. Die Plattform nutzt ein Skills-System, das dem LLM präzise Constraints, CSS-Vorlagen und Layout-Regeln mitgibt, sodass die generierten HTML/CSS-Outputs direkt in druckfertige PDFs konvertiert werden können.

## Glossary

- **Layout_Generator**: Das Spring Boot Backend-System, das HTML/CSS-Layouts aus Text-Prompts oder Referenzbildern generiert
- **Skills_System**: Markdown-basierte Prompt-Bausteine, die dem LLM Design-Constraints und Regeln mitgeben
- **Skill**: Eine einzelne Markdown-Datei mit Anweisungen für das LLM (z.B. LAYOUT_PRINCIPLES, TYPOGRAPHY)
- **Skill_Assembler**: Komponente, die Skills basierend auf Request-Optionen zusammenstellt und priorisiert
- **Agent_Orchestrator**: Koordiniert die Pipeline-Ausführung für Layout-Generierung
- **Pipeline**: Ausführungsstrategie für LLM-Calls (Linear, Iterativ, Parallel)
- **Gemini_Client**: HTTP-Client für die Google Gemini API
- **Vision_Analyzer**: Komponente für LLM-basierte Bildanalyse via Gemini Vision
- **LLM_Logger**: System zur Protokollierung aller LLM-Interaktionen in JSONL-Dateien
- **Product_Service**: Service zum Laden und Filtern von Produktdaten aus products.json
- **PDF_Generator**: Puppeteer-Bridge zur PDF-Erstellung aus HTML/CSS
- **ColorPalette**: Extrahierte Farbwerte aus einem Referenzbild
- **MoodAnalysis**: Stimmungsanalyse eines Referenzbildes (premium, eco, dynamic, etc.)
- **LayoutHints**: Layout-Empfehlungen basierend auf Bildanalyse (Grid-Typ, Dichte, Fokusbereich)

## Requirements

### Requirement 1: Produktdaten-Verwaltung

**User Story:** Als Benutzer möchte ich auf Produktdaten zugreifen können, damit ich diese für die Katalog-Generierung verwenden kann.

#### Acceptance Criteria

1. WHEN the Layout_Generator starts, THE Product_Service SHALL load all products from products.json into memory
2. WHEN a user requests products via GET /api/v1/products, THE Layout_Generator SHALL return a paginated list with filtering options for category and series
3. WHEN a user requests a single product via GET /api/v1/products/{id}, THE Layout_Generator SHALL return the complete product object including id, name, description, category, series, specs, and imageUrl
4. IF a requested product ID does not exist, THEN THE Layout_Generator SHALL return HTTP 404 Not Found
5. WHEN a user searches via GET /api/v1/products/search?q={query}, THE Layout_Generator SHALL perform case-insensitive full-text search across name and description fields
6. WHEN a user requests categories via GET /api/v1/products/categories, THE Layout_Generator SHALL return a sorted list of unique categories
7. WHEN a user requests series via GET /api/v1/products/series, THE Layout_Generator SHALL return a sorted list of unique series names

### Requirement 2: Text-to-Layout Generierung

**User Story:** Als Benutzer möchte ich aus einem Text-Prompt und ausgewählten Produkten ein professionelles pixelgenaues Layout generieren können.

#### Acceptance Criteria

1. WHEN a user sends POST /api/v1/layouts/generate/text with prompt and productIds, THE Layout_Generator SHALL generate HTML/CSS layout using the assembled skills and product data
2. IF the prompt is empty or missing, THEN THE Layout_Generator SHALL return HTTP 400 Bad Request
3. WHEN options.style is specified (e.g., "eco", "premium"), THE Skill_Assembler SHALL include the corresponding style skill (STYLE_ECO, STYLE_PREMIUM)
4. WHEN options.pageFormat is specified (e.g., "A4", "DL"), THE Skill_Assembler SHALL include the corresponding format skill
5. WHEN options.variantCount is greater than 1, THE Agent_Orchestrator SHALL use ParallelPipeline to generate multiple layout variants
6. WHEN options.variantCount is 1 or not specified, THE Agent_Orchestrator SHALL use LinearPipeline for single layout generation
7. THE Layout_Generator SHALL return a LayoutResponse containing id, status, generatedAt, pageFormat, variants array with html/css, and metadata including skillsUsed and generationTimeMs

### Requirement 3: Image-to-Layout Generierung (URL-basiert)

**User Story:** Als Benutzer möchte ich ein Referenzbild per URL angeben können, damit das Layout dessen Stil und Farbpalette übernimmt.

#### Acceptance Criteria

1. WHEN a user sends POST /api/v1/layouts/generate/image with imageUrl and productIds, THE Layout_Generator SHALL first analyze the image via Vision_Analyzer
2. WHEN the imageUrl is reachable, THE Vision_Analyzer SHALL extract ColorPalette, MoodAnalysis, and LayoutHints using Gemini Vision API
3. IF the imageUrl is not reachable within 5 seconds, THEN THE Layout_Generator SHALL activate placeholder mode and continue generation without image analysis
4. WHEN options.extractColors is true, THE Vision_Analyzer SHALL extract primary, secondary, accent, neutralLight, and neutralDark colors
5. WHEN options.analyzeMood is true, THE Vision_Analyzer SHALL determine mood type (premium, eco, dynamic, technical, modern) with confidence score and keywords
6. WHEN options.analyzeLayout is true, THE Vision_Analyzer SHALL extract gridType, density, focusArea, and suggestedColumns
7. THE Layout_Generator SHALL include imageAnalysis in the response containing colorPalette and mood when image analysis was performed
8. WHEN placeholderMode is active, THE Layout_Generator SHALL set placeholdersUsed=true in the response

### Requirement 4: Bild-Upload für Image-to-Layout

**User Story:** Als Benutzer möchte ich ein Referenzbild direkt hochladen können, damit ich keine externe URL benötige.

#### Acceptance Criteria

1. WHEN a user sends POST /api/v1/images/upload with a multipart file, THE Layout_Generator SHALL accept the image and store it temporarily
2. THE Layout_Generator SHALL accept image formats: JPEG, PNG, WebP with maximum file size of 10MB
3. IF the file format is not supported, THEN THE Layout_Generator SHALL return HTTP 400 Bad Request with error message
4. IF the file size exceeds 10MB, THEN THE Layout_Generator SHALL return HTTP 413 Payload Too Large
5. WHEN an image is successfully uploaded, THE Layout_Generator SHALL return a temporary imageId and a generated imageUrl valid for 24 hours
6. WHEN a user sends POST /api/v1/layouts/generate/image with the returned imageUrl, THE Vision_Analyzer SHALL analyze the uploaded image
7. THE Layout_Generator SHALL automatically delete uploaded images after 24 hours

### Requirement 5: Skills-System

**User Story:** Als Benutzer möchte ich die verfügbaren Skills einsehen können, damit ich verstehe, welche Design-Optionen verfügbar sind.

#### Acceptance Criteria

1. WHEN the Layout_Generator starts, THE Skill_Loader SHALL load all markdown files from the skills directory including subdirectories
2. THE Skill_Loader SHALL extract skill name from filename (e.g., PRODUCT_PAGE.md → PRODUCT_PAGE)
3. THE Skill_Loader SHALL extract category from directory path (e.g., skills/core/X.md → category="core")
4. THE Skill_Loader SHALL parse metadata including dependencies and priority from skill file headers
5. WHEN a user requests GET /api/v1/skills, THE Layout_Generator SHALL return an array of skill metadata (name, category, dependencies, priority)
6. WHEN a user requests GET /api/v1/skills/{category}, THE Layout_Generator SHALL return only skills of that category
7. IF the requested category does not exist, THEN THE Layout_Generator SHALL return HTTP 404 Not Found
8. WHEN a user requests GET /api/v1/skills/prompts/examples, THE Layout_Generator SHALL return example prompts from example-prompts.json

### Requirement 6: Skill-Assembly

**User Story:** Als System möchte ich Skills basierend auf Request-Optionen automatisch zusammenstellen, damit das LLM optimale Anweisungen erhält.

#### Acceptance Criteria

1. WHEN assembling skills, THE Skill_Assembler SHALL always include MASTER_SKILL first
2. WHEN assembling skills, THE Skill_Assembler SHALL resolve all dependencies recursively (e.g., PRODUCT_PAGE depends on LAYOUT_PRINCIPLES and TYPOGRAPHY)
3. WHEN assembling skills, THE Skill_Assembler SHALL sort skills by priority (higher priority first) after dependency resolution
4. WHEN assembling skills, THE Skill_Assembler SHALL merge all skill contents into a single system instruction
5. WHEN options.style is specified, THE Skill_Assembler SHALL include the corresponding STYLE_* skill
6. WHEN options.pageFormat is specified, THE Skill_Assembler SHALL include the corresponding FORMAT_* skill
7. WHEN imageAnalysis is available, THE Skill_Assembler SHALL include relevant image-analysis skills

### Requirement 7: Agent-Orchestrierung

**User Story:** Als System möchte ich verschiedene Pipeline-Strategien für unterschiedliche Generierungsanforderungen nutzen können.

#### Acceptance Criteria

1. WHEN variantCount equals 1 and no complexStrategy flag, THE Agent_Orchestrator SHALL select LinearPipeline
2. WHEN complexStrategy flag is true, THE Agent_Orchestrator SHALL select IterativePipeline with validation and retry logic
3. WHEN variantCount is greater than 1, THE Agent_Orchestrator SHALL select ParallelPipeline
4. WHEN imageUrl is provided, THE Agent_Orchestrator SHALL include ImageAnalysisStep in the pipeline
5. THE Agent_Orchestrator SHALL generate a unique pipelineId for each execution
6. WHEN LinearPipeline executes, THE Pipeline SHALL run PromptAssemblyStep followed by LayoutGenerationStep
7. WHEN IterativePipeline executes and validation fails, THE Pipeline SHALL retry with CorrectionStep up to maxRetries (default 3)
8. IF IterativePipeline exceeds maxRetries, THEN THE Pipeline SHALL execute FallbackStep
9. WHEN ParallelPipeline executes, THE Pipeline SHALL run N parallel LayoutGenerationSteps and aggregate results

### Requirement 8: Gemini-Integration

**User Story:** Als System möchte ich mit der Gemini API kommunizieren können, um Layouts und Bildanalysen zu generieren.

#### Acceptance Criteria

1. THE Gemini_Client SHALL send POST requests to the Gemini API with correct authentication header (x-goog-api-key)
2. WHEN generating layouts, THE Gemini_Client SHALL use model "gemini-2.5-flash" by default
3. WHEN analyzing images, THE Gemini_Client SHALL use model "gemini-2.5-pro-vision"
4. WHEN generating complex layouts, THE Gemini_Client SHALL use model "gemini-2.5-pro"
5. THE Gemini_Client SHALL include systemInstruction, userPrompt, and responseSchema in the request
6. THE Gemini_Client SHALL map the response to GeminiResponse including text content and token usage
7. IF the Gemini API returns an error, THEN THE Gemini_Client SHALL throw appropriate exception with error details
8. THE Gemini_Client SHALL respect configured timeout (default 60 seconds)

### Requirement 9: Layout-Lifecycle-Management

**User Story:** Als Benutzer möchte ich generierte Layouts speichern, abrufen, aktualisieren und löschen können. Gespeichert wird auf dem Servier im Dateiverzeichnis. Es wird keine DB verwendet.

#### Acceptance Criteria

1. WHEN a layout is generated, THE Layout_Generator SHALL assign a unique UUID and store it in memory
2. WHEN a user requests GET /api/v1/layouts/{id}, THE Layout_Generator SHALL return the stored layout
3. IF the requested layout ID does not exist, THEN THE Layout_Generator SHALL return HTTP 404 Not Found
4. WHEN a user sends PUT /api/v1/layouts/{id} with updated html/css, THE Layout_Generator SHALL update the stored layout
5. WHEN a user sends DELETE /api/v1/layouts/{id}, THE Layout_Generator SHALL remove the layout and return HTTP 204 No Content
6. WHEN a user requests GET /api/v1/layouts/{id}/variants, THE Layout_Generator SHALL return all variants of the layout

### Requirement 10: LLM-Logging

**User Story:** Als Entwickler möchte ich alle LLM-Interaktionen protokolliert haben, damit ich Prompts debuggen und optimieren kann.

#### Acceptance Criteria

1. WHEN an LLM request is about to be sent, THE LLM_Logger SHALL log a REQUEST entry with timestamp, requestId, pipelineId, stepName, model, systemPrompt, userPrompt, and inputTokensEstimate
2. WHEN an LLM response is received, THE LLM_Logger SHALL log a RESPONSE entry with timestamp, requestId, durationMs, status, outputTokens, and response content
3. THE LLM_Logger SHALL correlate request and response entries using the same requestId
4. THE LLM_Log_Writer SHALL write entries to daily JSONL files with pattern {date}_llm.jsonl
5. THE LLM_Log_Writer SHALL write one JSON object per line without embedded newlines
6. WHEN the date changes, THE LLM_Log_Writer SHALL create a new log file
7. THE LLM_Log_Writer SHALL append to existing files (not overwrite)
8. IF an LLM call results in an error, THEN THE LLM_Logger SHALL log errorType and errorMessage in the RESPONSE entry

### Requirement 11: PDF-Generierung

**User Story:** Als Benutzer möchte ich aus einem Layout ein druckfertiges PDF generieren können.

#### Acceptance Criteria

1. WHEN a user sends POST /api/v1/pdf/generate with layoutId and printPreset, THE PDF_Generator SHALL create a PDF from the layout's HTML/CSS
2. THE PDF_Generator SHALL support print presets: screen (72 DPI), print-standard (150 DPI), print-professional (300 DPI, 3mm bleed), print-premium (300 DPI, 5mm bleed, crop marks)
3. THE PDF_Generator SHALL communicate with Puppeteer via stdin/stdout JSON protocol
4. WHEN PDF generation succeeds, THE Layout_Generator SHALL return pdfId and downloadUrl
5. WHEN a user requests GET /api/v1/pdf/{id}/download, THE Layout_Generator SHALL return the PDF file with appropriate Content-Type header
6. IF the referenced layoutId does not exist, THEN THE Layout_Generator SHALL return HTTP 404 Not Found

### Requirement 12: Validierung und Sanitization

**User Story:** Als System möchte ich generierte Inhalte validieren und bereinigen, damit nur sichere und valide Outputs produziert werden.

#### Acceptance Criteria

1. THE Html_Sanitizer SHALL remove all script tags from generated HTML
2. THE Html_Sanitizer SHALL remove all event handler attributes (onclick, onload, etc.) from generated HTML
3. THE Html_Sanitizer SHALL preserve structural elements and CSS classes
4. THE Css_Validator SHALL validate CSS syntax including balanced brackets
5. THE Css_Validator SHALL accept print-specific units (mm, cm, pt)
6. THE Color_Utils SHALL validate hex color codes (3 and 6 digit formats)
7. THE Color_Utils SHALL calculate WCAG contrast ratios between two colors

### Requirement 13: Konfiguration

**User Story:** Als Entwickler möchte ich das System über application.yml konfigurieren können.

#### Acceptance Criteria

1. THE Layout_Generator SHALL read Gemini API key from environment variable GEMINI_API_KEY
2. THE Layout_Generator SHALL support configuration profiles: dev, prod, test
3. THE Layout_Generator SHALL allow configuration of: gemini timeout, max retries, default model, puppeteer paths, LLM logging directory, variant count limits
4. WHEN catalogforge.logging.llm.enabled is false, THE LLM_Logger SHALL not write log entries
5. WHEN catalogforge.layout.fallback-enabled is true, THE IterativePipeline SHALL use FallbackStep on failure

### Requirement 14: Fehlerbehandlung

**User Story:** Als Benutzer möchte ich aussagekräftige Fehlermeldungen erhalten, wenn etwas schiefgeht.

#### Acceptance Criteria

1. WHEN a validation error occurs, THE Layout_Generator SHALL return HTTP 400 with ErrorResponse containing error code and message
2. WHEN a resource is not found, THE Layout_Generator SHALL return HTTP 404 with ErrorResponse
3. WHEN the Gemini API fails, THE Layout_Generator SHALL return HTTP 502 Bad Gateway with error details
4. WHEN an internal error occurs, THE Layout_Generator SHALL return HTTP 500 with generic error message (no stack traces)
5. THE Global_Exception_Handler SHALL log all exceptions with full stack traces server-side
6. THE ErrorResponse SHALL contain: timestamp, status, error, message, and path
