# CatalogForge

> KI-gestützte Katalog- und Flyer-Generierung: Von Text-Prompts und Referenzbildern zu druckfertigen HTML/CSS-Layouts und PDFs.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.3-blue?logo=typescript)
![Gemini](https://img.shields.io/badge/Gemini-API-purple?logo=google)

---

## Inhaltsverzeichnis

- [Systemarchitektur](#systemarchitektur)
- [Request Flow](#request-flow)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Projektstruktur](#projektstruktur)
- [Agent Framework](#agent-framework)
- [Skills System](#skills-system)
- [Frontend Architektur](#frontend-architektur)
- [API Referenz](#api-referenz)
- [Testing](#testing)
- [Bekannte Einschränkungen](#bekannte-einschränkungen)
- [Screenshots](#screenshots)

---

## Systemarchitektur

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                   FRONTEND                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │  Dashboard  │  │   Wizard    │  │   Preview   │  │  Skill/Prompt Explorer  │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └────────────┬────────────┘ │
│         │                │                │                      │              │
│         └────────────────┴────────────────┴──────────────────────┘              │
│                                    │                                             │
│                          React Query + Zustand                                   │
│                                    │                                             │
└────────────────────────────────────┼─────────────────────────────────────────────┘
                                     │ HTTP/REST
                                     ▼
┌────────────────────────────────────┴─────────────────────────────────────────────┐
│                                   BACKEND                                        │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                           REST Controllers                                   │ │
│  │   /products  │  /layouts  │  /skills  │  /pdf  │  /images                   │ │
│  └───────┬──────────────┬─────────────┬──────────────┬──────────────┬──────────┘ │
│          │              │             │              │              │            │
│          ▼              ▼             ▼              ▼              ▼            │
│  ┌──────────────┐ ┌───────────────────────┐ ┌─────────────┐ ┌─────────────────┐  │
│  │   Product    │ │   Layout Generation   │ │   Skills    │ │  PDF Generation │  │
│  │   Service    │ │       Service         │ │   Service   │ │     Service     │  │
│  └──────┬───────┘ └───────────┬───────────┘ └──────┬──────┘ └────────┬────────┘  │
│         │                     │                    │                 │           │
│         │                     ▼                    │                 │           │
│         │         ┌───────────────────────┐        │                 │           │
│         │         │   Agent Orchestrator  │◄───────┘                 │           │
│         │         │  ┌─────────────────┐  │                          │           │
│         │         │  │ Pipeline Steps  │  │                          │           │
│         │         │  │ • ImageAnalysis │  │                          │           │
│         │         │  │ • SkillAssembly │  │                          │           │
│         │         │  │ • Generation    │  │                          │           │
│         │         │  │ • Validation    │  │                          │           │
│         │         │  └─────────────────┘  │                          │           │
│         │         └───────────┬───────────┘                          │           │
│         │                     │                                      │           │
│         │                     ▼                                      ▼           │
│         │         ┌───────────────────────┐              ┌───────────────────┐   │
│         │         │    Gemini Client      │              │  Puppeteer Bridge │   │
│         │         │  (Vision + Text LLM)  │              │   (Node.js PDF)   │   │
│         │         └───────────┬───────────┘              └─────────┬─────────┘   │
│         │                     │                                    │             │
└─────────┼─────────────────────┼────────────────────────────────────┼─────────────┘
          │                     │                                    │
          ▼                     ▼                                    ▼
   ┌─────────────┐      ┌─────────────┐                      ┌─────────────┐
   │ products.   │      │  Google     │                      │  Headless   │
   │   json      │      │  Gemini API │                      │   Chrome    │
   └─────────────┘      └─────────────┘                      └─────────────┘
```

CatalogForge ist eine Full-Stack-Anwendung mit klarer Trennung zwischen React-Frontend und Spring Boot-Backend. Das Backend orchestriert LLM-Aufrufe über ein eigenes Agent-Framework und generiert PDFs via Puppeteer/Chrome.

---

## Request Flow

### Text-to-Layout Generation

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant FE as Frontend
    participant BE as Backend
    participant AO as Agent Orchestrator
    participant SA as Skill Assembler
    participant GC as Gemini Client
    participant G as Gemini API

    U->>FE: Produkte + Prompt + Optionen
    FE->>BE: POST /api/v1/layouts/generate/text
    BE->>AO: execute(AgentContext)
    
    AO->>AO: selectStrategy(context)
    Note over AO: Simple/Complex/MultiVariant
    
    AO->>SA: assemblePrompt(products, options)
    SA->>SA: loadSkills(core + format + style)
    SA-->>AO: assembledPrompt
    
    loop für jede Variante
        AO->>GC: generateContent(prompt)
        GC->>G: POST /generateContent
        G-->>GC: HTML + CSS Response
        GC-->>AO: LayoutVariant
        AO->>AO: validateOutput(html, css)
    end
    
    AO-->>BE: Layout mit Varianten
    BE-->>FE: LayoutResponse (JSON)
    FE->>FE: Render in iframe
    FE-->>U: Layout-Vorschau
```

### Image-to-Layout Generation

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant FE as Frontend
    participant BE as Backend
    participant AO as Agent Orchestrator
    participant IA as Image Analysis Step
    participant GV as Gemini Vision
    participant GC as Gemini Client

    U->>FE: Bild + Produkte + Prompt
    FE->>BE: POST /api/v1/layouts/generate/image
    BE->>AO: execute(AgentContext mit Bild)
    
    AO->>IA: analyzeImage(base64, mimeType)
    IA->>GV: analyzeWithVision(image)
    GV-->>IA: ImageAnalysisResult
    Note over IA: ColorPalette, Mood, LayoutHints
    
    IA-->>AO: context.withImageAnalysis()
    
    AO->>AO: assemblePrompt + imageHints
    AO->>GC: generateContent(enrichedPrompt)
    GC-->>AO: Layout mit Bildstil
    
    AO-->>BE: Layout
    BE-->>FE: LayoutResponse
    FE-->>U: Stilisierte Vorschau
```

### PDF Export

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant FE as Frontend
    participant BE as Backend
    participant PS as PDF Service
    participant PB as Puppeteer Bridge
    participant C as Chrome

    U->>FE: "PDF exportieren" klicken
    FE->>BE: POST /api/v1/pdf/generate
    Note over FE,BE: {layoutId, variantId, preset}
    
    BE->>PS: generatePdf(layout, preset)
    PS->>PS: buildHtmlDocument(html, css)
    PS->>PB: renderToPdf(html, options)
    
    PB->>C: Launch headless
    C->>C: Render HTML
    C->>C: Print to PDF
    C-->>PB: PDF bytes
    
    PB-->>PS: PDF file
    PS-->>BE: {pdfId, downloadUrl}
    BE-->>FE: PdfResponse
    
    FE->>BE: GET /api/v1/pdf/{id}/download
    BE-->>FE: application/pdf
    FE-->>U: Download startet
```

---

## Tech Stack

### Backend

| Komponente | Technologie | Version | Zweck |
|------------|-------------|---------|-------|
| Runtime | Java | 21 LTS | Records, Pattern Matching, Virtual Threads |
| Framework | Spring Boot | 3.4.1 | REST API, WebFlux |
| Build | Gradle | 8.x | Kotlin DSL |
| LLM | Google Gemini | API | Text + Vision Generation |
| PDF | Puppeteer | Node.js | Headless Chrome Rendering |

### Frontend

| Komponente | Technologie | Version | Zweck |
|------------|-------------|---------|-------|
| UI | React | 18.2 | Component Library |
| Language | TypeScript | 5.3 | Type Safety |
| Build | Vite | 5.1 | Dev Server + Bundling |
| Styling | Tailwind CSS | 3.4 | Utility-First CSS |
| Server State | React Query | 5.24 | API Caching |
| Client State | Zustand | 4.5 | Wizard State |
| Charts | Recharts | 2.12 | Dashboard Visualisierungen |

### Testing

| Stack | Backend | Frontend |
|-------|---------|----------|
| Framework | JUnit 5 | Vitest |
| Mocking | Mockito | - |
| Assertions | AssertJ | Testing Library |
| Property-Based | jqwik | fast-check |

---

## Quick Start

### Voraussetzungen

- Java 21+
- Node.js 18+ (für Frontend + Puppeteer)
- Google Gemini API Key

### 1. Repository klonen

```bash
git clone <repository-url>
cd catalogforge
```

### 2. Backend starten

```bash
cd catForge-backend

# Environment konfigurieren
cp .env.example .env
# GEMINI_API_KEY in .env eintragen

# Starten
./gradlew bootRun
```

Backend läuft auf `http://localhost:8080`

### 3. Frontend starten

```bash
cd catForge-frontend

# Dependencies installieren
npm install

# Dev Server starten
npm run dev
```

Frontend läuft auf `http://localhost:3000` (Proxy zu Backend konfiguriert)

### 4. Anwendung öffnen

Browser öffnen: `http://localhost:3000`

### Wichtige Befehle

```bash
# Backend
./gradlew bootRun              # Starten
./gradlew test                 # Alle Tests
./gradlew unitTest             # Unit Tests
./gradlew propertyTest         # Property-Based Tests

# Frontend
npm run dev                    # Dev Server
npm run build                  # Production Build
npm run test                   # Tests (single run)
npm run test:coverage          # Coverage Report
```

---

## Projektstruktur


```
catalogforge/
├── catForge-backend/                 # Spring Boot Backend
│   ├── src/main/java/com/catalogforge/
│   │   ├── agent/                    # Agent Framework
│   │   │   ├── steps/                # Pipeline Steps
│   │   │   └── strategies/           # Simple, Complex, MultiVariant
│   │   ├── config/                   # Spring Configuration
│   │   ├── controller/               # REST Endpoints
│   │   ├── gemini/                   # Gemini API Client
│   │   ├── model/                    # Domain Models (Records)
│   │   ├── pdf/                      # Puppeteer Bridge
│   │   ├── service/                  # Business Logic
│   │   ├── skill/                    # Skill Loading
│   │   └── util/                     # Validators, Sanitizers
│   ├── src/main/resources/
│   │   ├── data/products.json        # Produktkatalog
│   │   └── skills/                   # Prompt Engineering
│   │       ├── core/                 # MASTER_SKILL, TYPOGRAPHY, etc.
│   │       ├── formats/              # A4, A5, DL, A6, SQUARE
│   │       └── styles/               # MODERN, TECHNICAL, PREMIUM, etc.
│   └── scripts/
│       └── pdf-generator.js          # Puppeteer Script
│
├── catForge-frontend/                # React Frontend
│   ├── src/
│   │   ├── api/                      # API Client Layer
│   │   ├── components/
│   │   │   ├── ui/                   # Button, Card, Modal, etc.
│   │   │   ├── charts/               # PieChart, BarChart, StatCard
│   │   │   ├── layout/               # AppLayout, Sidebar, Header
│   │   │   └── features/             # Feature Components
│   │   │       ├── wizard/           # ProductSelector, LayoutOptions
│   │   │       ├── preview/          # LayoutPreview, VariantSelector
│   │   │       ├── skills/           # SkillCard, SkillDetail
│   │   │       └── prompts/          # PromptCard, PromptDetail
│   │   ├── pages/                    # Route Pages
│   │   │   ├── Dashboard/
│   │   │   ├── CatalogWizard/
│   │   │   ├── CatalogPreview/
│   │   │   ├── SkillExplorer/
│   │   │   └── PromptExplorer/
│   │   ├── hooks/                    # useProducts, useLayouts, etc.
│   │   ├── store/                    # Zustand Stores
│   │   └── utils/                    # Helpers, Formatters
│   └── index.html
│
├── dev_doc/                          # Dokumentation & Screenshots
│   ├── api-analyse.md
│   ├── frontend-spezifikation.md
│   └── app-v1-screenshots/           # UI Screenshots
│
└── .kiro/                            # Kiro IDE Config
    ├── steering/                     # Coding Guidelines
    └── specs/                        # Feature Specs
```

---

## Agent Framework

```mermaid
flowchart TB
    subgraph Orchestrator["Agent Orchestrator"]
        direction TB
        SELECT[Strategy Selection]
        EXEC[Pipeline Execution]
    end

    subgraph Strategies["Pipeline Strategies"]
        SIMPLE[Simple Strategy<br/>1 Variante, schnell]
        COMPLEX[Complex Strategy<br/>Mehr LLM-Calls, bessere Qualität]
        MULTI[MultiVariant Strategy<br/>Parallele Generierung]
    end

    subgraph Pipelines["Pipeline Types"]
        LINEAR[Linear Pipeline<br/>Step → Step → Step]
        ITERATIVE[Iterative Pipeline<br/>Mit Retry & Correction]
        PARALLEL[Parallel Pipeline<br/>Concurrent Execution]
    end

    subgraph Steps["Pipeline Steps"]
        IMG[Image Analysis]
        SKILL[Skill Assembly]
        GEN[Layout Generation]
        VAL[Validation]
        CORR[Correction]
        FALL[Fallback]
    end

    SELECT --> SIMPLE & COMPLEX & MULTI
    SIMPLE --> LINEAR
    COMPLEX --> ITERATIVE
    MULTI --> PARALLEL
    
    LINEAR --> SKILL --> GEN --> VAL
    ITERATIVE --> SKILL --> GEN --> VAL --> CORR
    PARALLEL --> GEN
    
    IMG -.-> SKILL
```

### AgentContext (Immutable State)

```java
// Immutable Record mit withX() Pattern
public record AgentContext(
    String pipelineId,
    List<Product> products,
    LayoutOptions options,
    String userPrompt,
    String imageBase64,           // Optional: Referenzbild
    ImageAnalysisResult analysis, // Optional: Vision-Ergebnis
    String assembledPrompt,
    Layout generatedLayout,
    List<String> validationErrors,
    int retryCount
) {
    // Factory Methods
    public static AgentContext forTextGeneration(...) { }
    public static AgentContext forImageGeneration(...) { }
    
    // Immutable Updates
    public AgentContext withAssembledPrompt(String prompt) { }
    public AgentContext withGeneratedLayout(Layout layout) { }
    public AgentContext withValidationErrors(List<String> errors) { }
}
```

---

## Skills System

```mermaid
mindmap
  root((Skills))
    Core
      MASTER_SKILL
        Basis-Regeln
        HTML/CSS Constraints
      LAYOUT_PRINCIPLES
        Grid Systems
        Visual Hierarchy
      TYPOGRAPHY
        Font Pairing
        Readability
      COLOR_THEORY
        Contrast
        Accessibility
      SPACING
        Margins
        Padding
      GRID_SYSTEMS
        Columns
        Gutters
    Formats
      FORMAT_A4
        210×297mm
        Portrait
      FORMAT_A5
        148×210mm
        Compact
      FORMAT_DL
        99×210mm
        Flyer
      FORMAT_A6
        105×148mm
        Postcard
      FORMAT_SQUARE
        210×210mm
        Social
    Styles
      STYLE_MODERN
        Clean Lines
        Minimalist
      STYLE_TECHNICAL
        Data Focus
        Specs Tables
      STYLE_PREMIUM
        Luxury Feel
        White Space
      STYLE_ECO
        Green Tones
        Sustainability
      STYLE_DYNAMIC
        Bold Colors
        Energy
```

### Skill Composition

```
┌─────────────────────────────────────────────────────────────┐
│                    Assembled Prompt                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   MASTER_SKILL                       │    │
│  │  • HTML/CSS Output Rules                            │    │
│  │  • Print-Ready Constraints                          │    │
│  │  • Safety Zones & Bleed                             │    │
│  └─────────────────────────────────────────────────────┘    │
│                          +                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Core Skills (auto-included)             │    │
│  │  TYPOGRAPHY + COLOR_THEORY + SPACING + GRID         │    │
│  └─────────────────────────────────────────────────────┘    │
│                          +                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   FORMAT_A4                          │    │
│  │  • Dimensions: 210×297mm                            │    │
│  │  • Safe Zone: 10mm margins                          │    │
│  │  • Bleed: 3mm                                       │    │
│  └─────────────────────────────────────────────────────┘    │
│                          +                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  STYLE_MODERN                        │    │
│  │  • Clean typography                                 │    │
│  │  • Generous whitespace                              │    │
│  │  • Subtle color accents                             │    │
│  └─────────────────────────────────────────────────────┘    │
│                          +                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   User Prompt                        │    │
│  │  "Erstelle eine Produktseite für den eActros 600    │    │
│  │   mit Fokus auf Umweltvorteile..."                  │    │
│  └─────────────────────────────────────────────────────┘    │
│                          +                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  Product Data                        │    │
│  │  • Name, Description, Specs                         │    │
│  │  • Highlights, Price                                │    │
│  │  • Image URL                                        │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Frontend Architektur


### Routing & Pages

```mermaid
flowchart LR
    subgraph Routes["React Router"]
        ROOT["/"]
        SKILLS["/skills"]
        PROMPTS["/prompts"]
        WIZARD["/wizard"]
        PREVIEW["/preview/:id"]
    end

    subgraph Pages["Page Components"]
        DASH[DashboardPage<br/>Produktübersicht]
        SKILL_EXP[SkillExplorerPage<br/>Skills durchsuchen]
        PROMPT_EXP[PromptExplorerPage<br/>Beispiel-Prompts]
        WIZ[CatalogWizardPage<br/>4-Step Wizard]
        PREV[CatalogPreviewPage<br/>Layout + PDF Export]
    end

    ROOT --> DASH
    SKILLS --> SKILL_EXP
    PROMPTS --> PROMPT_EXP
    WIZARD --> WIZ
    PREVIEW --> PREV
```

### State Management

```mermaid
flowchart TB
    subgraph ServerState["Server State (React Query)"]
        PRODUCTS[(Products)]
        SKILLS_DATA[(Skills)]
        LAYOUTS[(Layouts)]
    end

    subgraph ClientState["Client State (Zustand)"]
        WIZARD_STORE[WizardStore]
    end

    subgraph WizardStore["Wizard Store State"]
        STEP[currentStep: 1-4]
        SELECTED[selectedProducts]
        OPTIONS[options: format, style, variants]
        PROMPT[prompt + referenceImage]
        RESULT[generatedLayout]
        CHAT[chatHistory]
    end

    PRODUCTS --> |useProducts| DASH_PAGE
    SKILLS_DATA --> |useSkills| SKILL_PAGE
    LAYOUTS --> |useLayouts| WIZARD_PAGE

    WIZARD_STORE --> STEP & SELECTED & OPTIONS & PROMPT & RESULT & CHAT
```

### Wizard Flow

```mermaid
flowchart LR
    subgraph Step1[1. Produkte]
        S1A[Suche & Filter]
        S1B[Multi-Select]
    end
    
    subgraph Step2[2. Optionen]
        S2A[Format wählen]
        S2B[Stil & Varianten]
    end
    
    subgraph Step3[3. Prompt]
        S3A[Text eingeben]
        S3B[Referenzbild optional]
    end
    
    subgraph Step4[4. Ergebnis]
        S4A[Layout Preview]
        S4B[PDF Export]
    end
    
    Step1 -->|Weiter| Step2
    Step2 -->|Weiter| Step3
    Step3 -->|Generieren| Step4
    
    Step2 -.->|Zurück| Step1
    Step3 -.->|Zurück| Step2
    Step4 -.->|Neu generieren| Step3
```

**Wizard UI Layout:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Katalog erstellen                                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│  [1. Produkte] → [2. Optionen] → [3. Prompt] → [4. Ergebnis]                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                                                                     │   │
│  │                     STEP CONTENT AREA                               │   │
│  │                                                                     │   │
│  │  Step 1: Produktauswahl mit Suche, Filter, Multi-Select            │   │
│  │  Step 2: Format (A4/A5/DL), Stil, Varianten-Slider                 │   │
│  │  Step 3: Prompt-Textarea, Bild-Upload, Chat-Interface              │   │
│  │  Step 4: Layout-Preview (iframe), Varianten-Tabs, PDF-Export       │   │
│  │                                                                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│                                           [← Zurück]  [Weiter →]            │
└─────────────────────────────────────────────────────────────────────────────┘
```

| Schritt | Funktion | Beschreibung |
|---------|----------|--------------|
| 1. Produkte | Auswahl | Suche, Filter nach Kategorie/Serie, Multi-Select |
| 2. Optionen | Konfiguration | Format (A4, A5, DL...), Stil, Varianten (1-5) |
| 3. Prompt | Eingabe | Text-Beschreibung, optionales Referenzbild, Chat |
| 4. Ergebnis | Vorschau | Layout-Preview, Varianten-Tabs, PDF Export |

---

## API Referenz

### Base URL: `/api/v1`

### Products API

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| `GET` | `/products` | Alle Produkte (optional: `?category=`, `?series=`) |
| `GET` | `/products/{id}` | Einzelnes Produkt |
| `GET` | `/products/categories` | Alle Kategorien |
| `GET` | `/products/series` | Alle Baureihen |
| `GET` | `/products/search?q={query}` | Volltextsuche |

### Layouts API

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| `POST` | `/layouts/generate/text` | Text-to-Layout |
| `POST` | `/layouts/generate/image` | Image-to-Layout |
| `GET` | `/layouts/{id}` | Layout abrufen |
| `GET` | `/layouts/{id}/variants` | Alle Varianten |
| `DELETE` | `/layouts/{id}` | Layout löschen |

### Skills API

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| `GET` | `/skills` | Alle Skills |
| `GET` | `/skills/categories` | Skill-Kategorien |
| `GET` | `/skills/{category}` | Skills einer Kategorie |
| `GET` | `/skills/prompts/examples` | Beispiel-Prompts |

### PDF API

| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| `POST` | `/pdf/generate` | PDF generieren |
| `GET` | `/pdf/{id}/download` | PDF herunterladen |
| `GET` | `/pdf/presets` | Print-Presets |

### Request/Response Beispiele

<details>
<summary><b>POST /layouts/generate/text</b></summary>

**Request:**
```json
{
  "productIds": [1, 6],
  "options": {
    "pageFormat": "A4",
    "style": "modern",
    "variantCount": 2,
    "includeSpecs": true
  },
  "prompt": "Erstelle eine Produktvergleichsseite für Actros L und eActros 600 mit Fokus auf Effizienz."
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "success",
  "generatedAt": "2025-01-15T14:30:00Z",
  "pageFormat": "A4",
  "variantCount": 2,
  "variants": [
    {
      "id": "variant-1",
      "html": "<div class=\"catalog-page\">...</div>",
      "css": ".catalog-page { ... }"
    },
    {
      "id": "variant-2",
      "html": "<div class=\"catalog-page\">...</div>",
      "css": ".catalog-page { ... }"
    }
  ]
}
```
</details>

<details>
<summary><b>POST /pdf/generate</b></summary>

**Request:**
```json
{
  "layoutId": "550e8400-e29b-41d4-a716-446655440000",
  "variantId": "variant-1",
  "preset": "print-professional"
}
```

**Response:**
```json
{
  "pdfId": "pdf-123",
  "downloadUrl": "/api/v1/pdf/pdf-123/download"
}
```
</details>

### Print Presets

| Preset | DPI | Bleed | Crop Marks | Verwendung |
|--------|-----|-------|------------|------------|
| `screen` | 72 | 0mm | ❌ | Bildschirmansicht |
| `print-standard` | 150 | 0mm | ❌ | Office-Druck |
| `print-professional` | 300 | 3mm | ✅ | Professioneller Druck |
| `print-premium` | 300 | 5mm | ✅ | Premium-Druck |

---

## Testing

### Backend Tests

```bash
# Alle Tests
./gradlew test

# Nach Tags filtern
./gradlew unitTest          # @Tag("unit")
./gradlew integrationTest   # @Tag("integration")
./gradlew propertyTest      # @Tag("property")

# Coverage Report
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

### Frontend Tests

```bash
# Single Run
npm run test

# Watch Mode
npm run test:watch

# Coverage
npm run test:coverage
```

### Test-Kategorien

| Kategorie | Backend | Frontend |
|-----------|---------|----------|
| Unit | JUnit 5 + Mockito | Vitest |
| Property-Based | jqwik | fast-check |
| Component | - | Testing Library |
| Integration | Spring Test | - |

---

## Bekannte Einschränkungen

### Aktueller Status (v1)

| Feature | Status | Anmerkung |
|---------|--------|-----------|
| Text-to-Layout | ✅ Funktioniert | - |
| Image-to-Layout | ✅ Funktioniert | - |
| Layout Preview | ✅ Funktioniert | - |
| PDF Export | ⚠️ Teilweise | Liefert aktuell leeres PDF |
| Produktbilder | ⚠️ Teilweise | Dummy-URLs, manche nicht erreichbar |
| DevContainer | 🚧 In Arbeit | Noch ungetestet, Frontend-Integration ausstehend |

### Bekannte Issues

1. **PDF Export**: Generiert aktuell leere PDFs - Puppeteer-Integration muss debuggt werden
2. **Produktbilder**: Verwenden Unsplash-Placeholder, einige URLs nicht mehr gültig
3. **DevContainer**: Konfiguration für Backend vorhanden, Frontend-Integration fehlt noch

---

## Screenshots

Screenshots der Anwendung (v1) befinden sich im Ordner `dev_doc/app-v1-screenshots/`.

Die Screenshots zeigen:
- Dashboard mit Produktübersicht und Charts
- Skill Explorer mit Kategorie-Filter
- Prompt Explorer mit Beispiel-Prompts
- Catalog Wizard (alle 4 Schritte)
- Layout Preview mit Varianten-Auswahl

---

## Weitere Dokumentation

| Dokument | Pfad | Inhalt |
|----------|------|--------|
| API Analyse | `dev_doc/api-analyse.md` | Vollständige API-Dokumentation |
| Frontend Spec | `dev_doc/frontend-spezifikation.md` | UI/UX Spezifikation |
| Steering Rules | `.kiro/steering/` | Coding Guidelines |

---

## Lizenz

Proprietär - Daimler Truck AG
