# CatalogForge REST API – Vollständige Analyse

### Basis-URL: `/api/v1`

---

## 1. Products API (`/api/v1/products`)

### Endpoints

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| GET | `/products` | Alle Produkte (optional gefiltert) |
| GET | `/products/{id}` | Einzelnes Produkt |
| GET | `/products/categories` | Alle Kategorien |
| GET | `/products/series` | Alle Baureihen |
| GET | `/products/search?q={query}` | Volltextsuche |

### GET `/products`

**Query-Parameter:**
```typescript
{
  category?: string;  // Filter nach Kategorie
  series?: string;    // Filter nach Baureihe
}
```

**Response:** `Product[]`

### GET `/products/{id}`

**Response:** `Product`

### GET `/products/categories`

**Response:** `string[]` (Set, alphabetisch sortiert)

### GET `/products/series`

**Response:** `string[]` (Set, alphabetisch sortiert)

### GET `/products/search?q={query}`

**Response:** `Product[]`

### Datenstruktur: Product

```typescript
interface Product {
  id: number;
  name: string;
  shortDescription: string;    // 1-2 Sätze für kompakte Layouts
  description: string;         // ~150 Wörter für Standard-Layouts
  longDescription: string;     // 300+ Wörter für Detail-Seiten
  category: string;
  series: string;
  specs: TechnicalData;        // Key-Value Map
  highlights: string[];        // Feature-Liste
  imageUrl: string;
  priceEur: number | null;
}

// TechnicalData serialisiert direkt als Map
type TechnicalData = Record<string, string>;
```

---

## 2. Layouts API (`/api/v1/layouts`)

### Endpoints

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/layouts/generate/text` | Text-to-Layout Generierung |
| POST | `/layouts/generate/image` | Image-to-Layout Generierung |
| GET | `/layouts/{id}` | Layout abrufen |
| PUT | `/layouts/{id}` | Layout aktualisieren |
| DELETE | `/layouts/{id}` | Layout löschen |
| GET | `/layouts/{id}/variants` | Alle Varianten |
| GET | `/layouts/{layoutId}/variants/{variantId}` | Einzelne Variante |

### POST `/layouts/generate/text`

**Request:** `TextToLayoutRequest`
```typescript
interface TextToLayoutRequest {
  productIds: number[];        // min. 1 Produkt
  options?: LayoutOptions;
  prompt: string;              // max. 5000 Zeichen, required
}

interface LayoutOptions {
  pageFormat?: string;         // "A4" | "A5" | "A6" | "DL" | "SQUARE", default: "A4"
  style?: string;              // "modern" | "technical" | "premium" | "eco" | "dynamic"
  variantCount?: number;       // 1-5, default: 1
  includeSpecs?: boolean;      // default: true
  complexStrategy?: boolean;   // default: false
}
```

**Response:** `LayoutResponse` (HTTP 201)

### POST `/layouts/generate/image`

**Request:** `ImageToLayoutRequest`
```typescript
interface ImageToLayoutRequest {
  productIds: number[];        // min. 1 Produkt
  options?: LayoutOptions;
  prompt?: string;             // max. 5000 Zeichen
  imageBase64: string;         // required, Base64-encoded image
  imageMimeType: string;       // required, z.B. "image/jpeg", "image/png"
}
```

**Response:** `LayoutResponse` (HTTP 201)

### GET `/layouts/{id}`

**Response:** `LayoutResponse`

### PUT `/layouts/{id}`

**Request:** `Layout` (vollständiges Layout-Objekt)

**Response:** `LayoutResponse`

### DELETE `/layouts/{id}`

**Response:** HTTP 204 No Content

### GET `/layouts/{id}/variants`

**Response:** `VariantResponse[]`

### GET `/layouts/{layoutId}/variants/{variantId}`

**Response:** `VariantResponse`

### Datenstrukturen

```typescript
interface LayoutResponse {
  id: string;                  // UUID
  status: string;              // "success" | "error"
  generatedAt: string;         // ISO 8601 timestamp
  pageFormat: string;          // "A4", "A5", etc.
  variants: VariantResponse[];
  variantCount: number;
}

interface VariantResponse {
  id: string;                  // UUID
  html: string;                // Generiertes HTML
  css: string;                 // Generiertes CSS
}

// Vollständiges Layout (intern/PUT)
interface Layout {
  id: string;
  status: string;
  generatedAt: string;
  pageFormat: PageFormat;
  imageAnalysis?: ImageAnalysisResult;
  variants: LayoutVariant[];
  metadata: LayoutMetadata;
}

interface PageFormat {
  name: string;                // "A4", "A5", "A6", "DL", "SQUARE"
  width: number;               // in mm
  height: number;              // in mm
  unit: string;                // "mm"
}

interface LayoutVariant {
  id: string;
  html: string;
  css: string;
}

interface LayoutMetadata {
  skillsUsed: string[];
  generationTimeMs: number;
  llmCallCount: number;
}

interface ImageAnalysisResult {
  colorPalette: ColorPalette;
  mood: MoodAnalysis;
  layoutHints: LayoutHints;
}

interface ColorPalette {
  primary: string;             // Hex, z.B. "#1a1a2e"
  secondary: string;
  accent: string;
  neutralLight: string;
  neutralDark: string;
}

interface MoodAnalysis {
  type: string;                // z.B. "premium", "dynamic", "eco"
  confidence: number;          // 0.0 - 1.0
  keywords: string[];
}

interface LayoutHints {
  gridType: string;            // z.B. "modular", "asymmetric"
  density: string;             // z.B. "low", "medium", "high"
  focusArea: string;           // z.B. "center", "left", "top"
  suggestedColumns: number;
}
```

---

## 3. Skills API (`/api/v1/skills`)

### Endpoints

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| GET | `/skills` | Alle Skills |
| GET | `/skills/categories` | Alle Skill-Kategorien |
| GET | `/skills/{category}` | Skills einer Kategorie |
| GET | `/skills/prompts/examples` | Beispiel-Prompts |
| POST | `/skills/reload` | Skills neu laden |

### GET `/skills`

**Response:** `Skill[]`

### GET `/skills/categories`

**Response:** `string[]` (Set)

### GET `/skills/{category}`

**Response:** `Skill[]`

### GET `/skills/prompts/examples`

**Response:** `ExamplePrompt[]`

### POST `/skills/reload`

**Response:** HTTP 200 OK (kein Body)

### Datenstrukturen

```typescript
interface Skill {
  name: string;                // z.B. "LAYOUT_PRINCIPLES"
  category: string;            // "core" | "styles" | "formats"
  content: string;             // Markdown-Inhalt
  dependencies: string[];      // Abhängige Skills
  priority: number;            // Sortierung (0 = höchste)
}

interface ExamplePrompt {
  id: string;
  title: string;
  prompt: string;
  skills: string[];
  style: string;
  format: string;
}
```

---

## 4. PDF API (`/api/v1/pdf`)

### Endpoints

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/pdf/generate` | PDF generieren |
| GET | `/pdf/{id}/download` | PDF herunterladen |
| GET | `/pdf/{id}` | PDF-Metadaten |
| DELETE | `/pdf/{id}` | PDF löschen |
| GET | `/pdf/presets` | Verfügbare Print-Presets |

### POST `/pdf/generate`

**Request:**
```typescript
interface PdfGenerateRequest {
  layoutId: string;            // required
  variantId?: string;          // optional, default: erste Variante
  preset?: string;             // default: "screen"
}
```

**Response:** (HTTP 201)
```typescript
{
  pdfId: string;
  downloadUrl: string;         // "/api/v1/pdf/{id}/download"
}
```

### GET `/pdf/{id}/download`

**Response:** Binary PDF (Content-Type: `application/pdf`)

### GET `/pdf/{id}`

**Response:**
```typescript
{
  id: string;
  layoutId: string;
  variantId: string;
  preset: string;
  downloadUrl: string;
}
```

### DELETE `/pdf/{id}`

**Response:** HTTP 204 No Content

### GET `/pdf/presets`

**Response:** `PrintPreset[]`

### Datenstrukturen

```typescript
interface PrintPreset {
  name: string;                // "screen" | "print-standard" | "print-professional" | "print-premium"
  description: string;
  dpi: number;                 // 72, 150, 300
  bleedMm: number;             // 0, 3, 5
  cropMarks: boolean;
}
```

**Verfügbare Presets:**

| Name | DPI | Bleed | Crop Marks | Beschreibung |
|------|-----|-------|------------|--------------|
| `screen` | 72 | 0mm | ❌ | Screen viewing |
| `print-standard` | 150 | 0mm | ❌ | Standard office printing |
| `print-professional` | 300 | 3mm | ✅ | Professional printing |
| `print-premium` | 300 | 5mm | ✅ | Premium printing with bleed |

---

## 5. Images API (`/api/v1/images`)

### Endpoints

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/images/upload` | Bild hochladen (Multipart) |
| POST | `/images/upload/base64` | Bild hochladen (Base64 JSON) |
| GET | `/images/{imageId}` | Bild abrufen |
| DELETE | `/images/{imageId}` | Bild löschen |

### POST `/images/upload`

**Request:** `multipart/form-data`
- `file`: Bilddatei (max. 10MB)

**Response:** `ImageUploadResponse` (HTTP 201)

### POST `/images/upload/base64`

**Request:**
```typescript
interface ImageUploadRequest {
  base64Data: string;
  mimeType: string;            // z.B. "image/jpeg"
  filename?: string;
}
```

**Response:** `ImageUploadResponse` (HTTP 201)

### GET `/images/{imageId}`

**Response:** Binary Image (Content-Type entsprechend MIME-Type)

### DELETE `/images/{imageId}`

**Response:** HTTP 204 No Content

### Datenstrukturen

```typescript
interface ImageUploadResponse {
  imageId: string;
  url: string;                 // "/api/v1/images/{imageId}"
  mimeType: string;
  expiresAt: string;           // ISO 8601, +24h
}
```

---

## 6. Error Response (Global)

Alle Fehler folgen diesem Format:

```typescript
interface ErrorResponse {
  timestamp: string;           // ISO 8601
  status: number;              // HTTP Status Code
  error: string;               // z.B. "Not Found", "Bad Request"
  message: string;             // Detaillierte Fehlermeldung
  path: string;                // Request-Pfad
}
```

**Beispiel:**
```json
{
  "timestamp": "2025-01-15T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Layout not found: abc-123",
  "path": "/api/v1/layouts/abc-123"
}
```

---

## Zusammenfassung für Frontend-Entwicklung

### Haupt-Workflows

1. **Produkt-Auswahl** → `GET /products` mit Filtern
2. **Text-to-Layout** → `POST /layouts/generate/text` mit Produkten + Prompt
3. **Image-to-Layout** → Bild hochladen → `POST /layouts/generate/image`
4. **Layout-Preview** → Varianten aus Response rendern (HTML/CSS)
5. **PDF-Export** → `POST /pdf/generate` → `GET /pdf/{id}/download`

### Wichtige Konstanten

**Page Formats:** `A4`, `A5`, `A6`, `DL`, `SQUARE`

**Styles:** `modern`, `technical`, `premium`, `eco`, `dynamic`

**Skill Categories:** `core`, `styles`, `formats`
