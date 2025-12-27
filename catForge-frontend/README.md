> ⚠️ **Hinweis: 100% KI-generiertes Projekt**
>
> Dieses Projekt wurde vollständig mit KI erstellt und dient als Test der **KIRO IDE** und deren **Spec-Driven Development** Ansatz. Die Spezifikationen wurden in mehreren Claude-Chat-Sessions erarbeitet (Context Engineering), anschließend wurde die Task-Liste aus dieser Spec mit Kiro generiert. Die Implementierung erfolgte mit **Opus 4.5**.

---

# CatalogForge Frontend

React-basierte Single-Page-Application für die KI-gestützte Katalog- und Flyer-Generierung. Transformiert Text-Prompts und Referenzbilder in professionelle, druckfertige HTML/CSS-Layouts.

## Quick Start

```bash
# Dependencies installieren
npm install

# Development Server starten (Port 3000)
npm run dev

# Tests ausführen
npm run test

# Production Build
npm run build
```

> **Voraussetzung:** Backend muss auf `http://localhost:8080` laufen (wird automatisch geproxied).

---

## Architektur-Übersicht

```mermaid
graph TB
    subgraph "Browser"
        App[App.tsx]
        Router[React Router]
        
        subgraph "Pages"
            Dashboard[Dashboard]
            Wizard[CatalogWizard]
            Preview[CatalogPreview]
            Skills[SkillExplorer]
            Prompts[PromptExplorer]
        end
        
        subgraph "State Management"
            RQ[React Query<br/>Server State]
            Zustand[Zustand<br/>Client State]
        end
        
        subgraph "API Layer"
            Client[API Client]
            Hooks[Custom Hooks]
        end
    end
    
    subgraph "Backend"
        API[REST API<br/>/api/v1/*]
    end
    
    App --> Router
    Router --> Pages
    Pages --> Hooks
    Hooks --> RQ
    Hooks --> Zustand
    RQ --> Client
    Client --> API
    
    style App fill:#3b82f6,color:#fff
    style RQ fill:#10b981,color:#fff
    style Zustand fill:#f59e0b,color:#fff
    style API fill:#6366f1,color:#fff
```

---

## Datenfluss

```mermaid
sequenceDiagram
    participant U as User
    participant W as Wizard Page
    participant S as Zustand Store
    participant H as useLayouts Hook
    participant RQ as React Query
    participant API as Backend API
    
    U->>W: Produkte auswählen
    W->>S: addProduct()
    U->>W: Optionen konfigurieren
    W->>S: setOptions()
    U->>W: Prompt eingeben + Generieren
    W->>H: useGenerateFromText()
    H->>RQ: mutateAsync()
    RQ->>API: POST /layouts/generate/text
    API-->>RQ: LayoutResponse
    RQ-->>H: data
    H->>S: setGeneratedLayout()
    S-->>W: Re-render mit Layout
    W-->>U: Layout-Varianten anzeigen
```

---

## Projektstruktur

```
src/
├── api/                 # API-Schicht
│   ├── client.ts        # Fetch-Wrapper mit Error Handling
│   ├── types.ts         # TypeScript Interfaces
│   ├── layouts.ts       # Layout-Generierung Endpoints
│   ├── products.ts      # Produktkatalog Endpoints
│   ├── skills.ts        # Skills-System Endpoints
│   ├── images.ts        # Bild-Upload Endpoints
│   └── pdf.ts           # PDF-Export Endpoints
│
├── components/
│   ├── charts/          # Recharts Visualisierungen
│   │   ├── BarChart.tsx
│   │   ├── PieChart.tsx
│   │   └── StatCard.tsx
│   │
│   ├── features/        # Feature-spezifische Komponenten
│   │   ├── preview/     # Layout-Vorschau
│   │   ├── products/    # Produktanzeige
│   │   ├── prompts/     # Prompt Explorer
│   │   ├── skills/      # Skills Explorer
│   │   └── wizard/      # Katalog-Wizard Steps
│   │
│   ├── layout/          # App Shell
│   │   ├── AppLayout.tsx
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── PageContainer.tsx
│   │
│   └── ui/              # Wiederverwendbare UI-Primitives
│       ├── Button.tsx
│       ├── Card.tsx
│       ├── Modal.tsx
│       ├── Input.tsx
│       └── ...
│
├── hooks/               # Custom React Hooks
│   ├── useLayouts.ts    # Layout-Generierung mit React Query
│   ├── useProducts.ts   # Produkt-Fetching
│   ├── useSkills.ts     # Skills-Fetching
│   ├── usePdf.ts        # PDF-Export
│   └── useDebounce.ts   # Input Debouncing
│
├── pages/               # Route Pages
│   ├── Dashboard/       # Übersicht mit Statistiken
│   ├── CatalogWizard/   # Multi-Step Wizard
│   ├── CatalogPreview/  # Layout-Vorschau + PDF-Export
│   ├── SkillExplorer/   # Skills durchsuchen
│   └── PromptExplorer/  # Prompt-Historie
│
├── store/               # Zustand Stores
│   └── wizardStore.ts   # Wizard State Management
│
├── utils/               # Utility Functions
│   ├── cn.ts            # Tailwind Class Merging
│   ├── formatters.ts    # Datum/Zahlen Formatierung
│   ├── validators.ts    # Form Validation
│   ├── filters.ts       # Produkt-Filterung
│   └── pageFormats.ts   # Druckformat Utilities
│
└── styles/
    └── globals.css      # Tailwind Base Styles
```

---

## Komponenten-Hierarchie

```mermaid
graph TD
    subgraph "App Shell"
        App[App.tsx]
        AppLayout[AppLayout]
        Sidebar[Sidebar]
        Header[Header]
    end
    
    subgraph "Pages"
        Dashboard[DashboardPage]
        Wizard[CatalogWizardPage]
        Preview[CatalogPreviewPage]
    end
    
    subgraph "Wizard Components"
        StepIndicator[StepIndicator]
        ProductSelector[ProductSelector]
        LayoutOptions[LayoutOptions]
        PromptInput[PromptInput]
        ImageUpload[ImageUpload]
        ChatInterface[ChatInterface]
    end
    
    subgraph "Preview Components"
        LayoutPreview[LayoutPreview]
        VariantSelector[VariantSelector]
    end
    
    subgraph "Shared UI"
        Button[Button]
        Card[Card]
        Modal[Modal]
        Spinner[Spinner]
    end
    
    App --> AppLayout
    AppLayout --> Sidebar
    AppLayout --> Header
    AppLayout --> Pages
    
    Wizard --> StepIndicator
    Wizard --> ProductSelector
    Wizard --> LayoutOptions
    Wizard --> PromptInput
    Wizard --> ImageUpload
    Wizard --> ChatInterface
    Wizard --> LayoutPreview
    Wizard --> VariantSelector
    
    Preview --> LayoutPreview
    Preview --> VariantSelector
    
    ProductSelector --> Card
    ProductSelector --> Button
    LayoutOptions --> Card
    ChatInterface --> Card
    ChatInterface --> Spinner
    
    style App fill:#3b82f6,color:#fff
    style Wizard fill:#10b981,color:#fff
    style Preview fill:#f59e0b,color:#fff
```

---

## State Management

### Server State (React Query)

```mermaid
graph LR
    subgraph "Query Keys"
        PK["['products']"]
        LK["['layouts', id]"]
        SK["['skills']"]
    end
    
    subgraph "Hooks"
        UP[useProducts]
        UL[useLayouts]
        US[useSkills]
    end
    
    subgraph "Cache"
        C[(React Query<br/>Cache)]
    end
    
    UP --> PK
    UL --> LK
    US --> SK
    
    PK --> C
    LK --> C
    SK --> C
    
    style C fill:#10b981,color:#fff
```

**Konfiguration:**
- `staleTime: 5 Minuten` – Daten gelten 5 Min. als frisch
- `retry: 1` – Ein Retry bei Fehlern
- Automatisches Cache-Update nach Mutations

### Client State (Zustand)

```mermaid
stateDiagram-v2
    [*] --> Step1: Wizard Start
    
    Step1: Produkte auswählen
    Step2: Optionen konfigurieren
    Step3: Prompt eingeben
    Step4: Ergebnis anzeigen
    
    Step1 --> Step2: nextStep()
    Step2 --> Step3: nextStep()
    Step3 --> Step4: handleGenerate()
    
    Step2 --> Step1: prevStep()
    Step3 --> Step2: prevStep()
    Step4 --> Step3: prevStep()
    
    Step4 --> [*]: handleFinish()
    
    note right of Step1
        selectedProducts[]
    end note
    
    note right of Step2
        options: LayoutOptions
    end note
    
    note right of Step3
        prompt: string
        referenceImage: base64
        chatHistory[]
    end note
    
    note right of Step4
        generatedLayout
        selectedVariantId
    end note
```

**Wizard Store Interface:**
```typescript
interface WizardState {
  // Navigation
  currentStep: 1 | 2 | 3 | 4;
  
  // Step 1: Products
  selectedProducts: Product[];
  
  // Step 2: Options
  options: LayoutOptions;
  
  // Step 3: Prompt
  prompt: string;
  referenceImage: { base64: string; mimeType: string } | null;
  chatHistory: ChatMessage[];
  
  // Step 4: Result
  generatedLayout: LayoutResponse | null;
  selectedVariantId: string | null;
  
  // Status
  isGenerating: boolean;
  error: string | null;
}
```

---

## API-Schicht

```mermaid
graph TB
    subgraph "Hooks Layer"
        H1[useProducts]
        H2[useLayouts]
        H3[useSkills]
        H4[usePdf]
    end
    
    subgraph "API Modules"
        A1[productsApi]
        A2[layoutsApi]
        A3[skillsApi]
        A4[pdfApi]
    end
    
    subgraph "Client"
        C[apiClient<br/>fetch wrapper]
    end
    
    subgraph "Backend Endpoints"
        E1["/api/v1/products"]
        E2["/api/v1/layouts"]
        E3["/api/v1/skills"]
        E4["/api/v1/pdf"]
    end
    
    H1 --> A1
    H2 --> A2
    H3 --> A3
    H4 --> A4
    
    A1 --> C
    A2 --> C
    A3 --> C
    A4 --> C
    
    C --> E1
    C --> E2
    C --> E3
    C --> E4
    
    style C fill:#6366f1,color:#fff
```

### Wichtige Endpoints

| Endpoint | Methode | Beschreibung |
|----------|---------|--------------|
| `/layouts/generate/text` | POST | Layout aus Text-Prompt generieren |
| `/layouts/generate/image` | POST | Layout aus Referenzbild generieren |
| `/layouts/{id}` | GET | Layout abrufen |
| `/products` | GET | Alle Produkte laden |
| `/skills` | GET | Verfügbare Skills laden |
| `/pdf/generate` | POST | PDF aus Layout generieren |

---

## Routing

```mermaid
graph LR
    subgraph "Routes"
        R1["/ → Dashboard"]
        R2["/wizard → CatalogWizard"]
        R3["/preview/:id → CatalogPreview"]
        R4["/skills → SkillExplorer"]
        R5["/prompts → PromptExplorer"]
        R6["/* → Redirect to /"]
    end
    
    style R1 fill:#3b82f6,color:#fff
    style R2 fill:#10b981,color:#fff
    style R3 fill:#f59e0b,color:#fff
```

**Features:**
- Lazy Loading aller Pages via `React.lazy()`
- Suspense Fallback mit Spinner
- Catch-all Route redirected zu Dashboard

---

## Tech Stack

| Kategorie | Technologie | Version |
|-----------|-------------|---------|
| **UI Library** | React | 18.2 |
| **Type Safety** | TypeScript | 5.3 |
| **Build Tool** | Vite | 5.1 |
| **Styling** | Tailwind CSS | 3.4 |
| **Routing** | React Router | 6.22 |
| **Server State** | React Query | 5.24 |
| **Client State** | Zustand | 4.5 |
| **Forms** | React Hook Form | 7.50 |
| **Charts** | Recharts | 2.12 |
| **Icons** | Lucide React | 0.330 |
| **Testing** | Vitest + Testing Library | 1.3 / 14.2 |

---

## Testing

```bash
# Alle Tests ausführen
npm run test

# Watch Mode
npm run test:watch

# Coverage Report
npm run test:coverage
```

### Test-Struktur

```
src/
├── api/
│   └── errors.test.ts           # API Error Handling
├── components/
│   ├── ui/
│   │   ├── Button.test.tsx
│   │   └── Badge.test.tsx
│   ├── layout/
│   │   └── Sidebar.test.tsx
│   └── features/
│       ├── wizard/wizard.test.ts
│       ├── skills/skills.test.ts
│       └── prompts/prompts.test.ts
├── store/
│   └── wizardStore.test.ts      # Zustand Store Tests
└── utils/
    ├── filters.test.ts
    ├── aggregations.test.ts
    └── pageFormats.test.ts
```

### Test-Patterns

```typescript
// Component Test mit Testing Library
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders children', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });
});

// Property-Based Test mit fast-check
import fc from 'fast-check';

describe('filterProducts', () => {
  it('returns subset of input', () => {
    fc.assert(
      fc.property(fc.array(productArbitrary), (products) => {
        const filtered = filterProducts(products, {});
        return filtered.length <= products.length;
      })
    );
  });
});
```

---

## Entwicklung

### Lokale Entwicklung

```bash
# Frontend starten
npm run dev

# Backend muss separat laufen auf Port 8080
# Vite proxied /api/* automatisch
```

### Code Conventions

**TypeScript:**
- Interfaces für Object Shapes
- Types für Unions
- Strict Mode aktiviert
- Kein `any`

**Components:**
- Functional Components mit Hooks
- Props Destructuring mit TypeScript Interface
- Co-located Tests (`Component.test.tsx`)

**Styling:**
- Tailwind CSS für alles
- `cn()` Utility für conditional Classes
- Keine Inline Styles

**State:**
- Server State → React Query
- Client State → Zustand
- Local UI State → useState

### Path Aliases

```typescript
// Statt relativer Pfade
import { Button } from '../../../components/ui/Button';

// Mit Alias
import { Button } from '@/components/ui';
```

Konfiguriert in `vite.config.ts` und `tsconfig.json`.

---

## Umgebungsvariablen

```bash
# .env.example
VITE_API_URL=/api/v1  # Optional, Default ist /api/v1
```

---

## Scripts

| Script | Beschreibung |
|--------|--------------|
| `npm run dev` | Development Server (Port 3000) |
| `npm run build` | Production Build |
| `npm run preview` | Preview Production Build |
| `npm run test` | Tests ausführen (single run) |
| `npm run test:watch` | Tests im Watch Mode |
| `npm run test:coverage` | Coverage Report |
| `npm run lint` | ESLint Check |

---

## Weiterführende Dokumentation

- [Frontend Spezifikation](../dev_doc/frontend-spezifikation.md)
- [API Analyse](../dev_doc/api-analyse.md)
- [Backend README](../catForge-backend/README.md)
