# CatalogForge Backend

Spring Boot 3.4 REST API für die KI-gestützte Generierung professioneller Produktkataloge und Flyer mit Google Gemini.

## Features

- **Text-to-Layout**: Generiert HTML/CSS-Layouts aus Textbeschreibungen
- **Image-to-Layout**: Analysiert Referenzbilder für Farbpaletten und Stimmung
- **Skills-System**: Modulares Prompt-Engineering mit wiederverwendbaren Skills
- **Agent Framework**: Flexible Pipelines (Linear, Iterativ, Parallel)
- **PDF-Export**: Druckfertige PDFs via Puppeteer mit verschiedenen Presets
- **Multi-Varianten**: Generiert mehrere Layout-Varianten parallel

## Architektur

```
┌─────────────────────────────────────────────────────────────┐
│                     REST Controllers                         │
│  (Products, Skills, Layouts, Images, PDF)                   │
├─────────────────────────────────────────────────────────────┤
│                      Services Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ ProductSvc  │  │  SkillsSvc  │  │ LayoutGenerationSvc │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                    Agent Framework                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              AgentOrchestrator                        │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────┐    │   │
│  │  │  Linear    │ │ Iterative  │ │   Parallel     │    │   │
│  │  │  Pipeline  │ │  Pipeline  │ │   Pipeline     │    │   │
│  │  └────────────┘ └────────────┘ └────────────────┘    │   │
│  └──────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                    External Services                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │GeminiClient │  │ LLM Logger  │  │  PuppeteerBridge    │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Voraussetzungen

### Lokal
- Java 21
- Node.js 20+ (für PDF-Generierung)
- Google Gemini API Key

### Dev Container (empfohlen)
- Docker
- VS Code mit Dev Containers Extension

## Schnellstart

### Option 1: Dev Container (empfohlen)

1. Repository öffnen in VS Code
2. `Cmd/Ctrl + Shift + P` → "Dev Containers: Reopen in Container"
3. Warten bis der Container gebaut ist
4. Gemini API Key setzen:
   ```bash
   export GEMINI_API_KEY=your-api-key
   ```
5. Anwendung starten:
   ```bash
   ./gradlew bootRun
   ```

### Option 2: Lokale Installation

1. **Java 21 installieren** (z.B. via SDKMAN):
   ```bash
   sdk install java 21-tem
   ```

2. **Node.js Dependencies installieren**:
   ```bash
   cd scripts
   npm install
   cd ..
   ```

3. **Umgebungsvariablen setzen**:
   ```bash
   cp .env.example .env
   # .env editieren und GEMINI_API_KEY setzen
   source .env
   ```

4. **Anwendung starten**:
   ```bash
   ./gradlew bootRun
   ```

Die API ist dann unter `http://localhost:8080` erreichbar.

## Konfiguration

### Umgebungsvariablen

| Variable | Beschreibung | Pflicht |
|----------|--------------|---------|
| `GEMINI_API_KEY` | Google Gemini API Key | Ja |

### application.yml

Wichtige Konfigurationsoptionen:

```yaml
catalogforge:
  gemini:
    api-key: ${GEMINI_API_KEY:}
    timeout-seconds: 60
    max-retries: 3
  
  layout:
    variant-count-default: 2
    max-variant-count: 5
    fallback-enabled: true
  
  images:
    expiration-hours: 24
```

## API-Endpoints

### Products
| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| GET | `/api/v1/products` | Alle Produkte (paginiert, filterbar) |
| GET | `/api/v1/products/{id}` | Einzelnes Produkt |
| GET | `/api/v1/products/categories` | Alle Kategorien |
| GET | `/api/v1/products/series` | Alle Serien |
| GET | `/api/v1/products/search?q={query}` | Volltextsuche |

### Skills
| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| GET | `/api/v1/skills` | Alle Skills |
| GET | `/api/v1/skills/{category}` | Skills nach Kategorie |
| GET | `/api/v1/skills/prompts/examples` | Beispiel-Prompts |

### Layouts
| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/api/v1/layouts/generate/text` | Text-to-Layout |
| POST | `/api/v1/layouts/generate/image` | Image-to-Layout |
| GET | `/api/v1/layouts/{id}` | Layout abrufen |
| PUT | `/api/v1/layouts/{id}` | Layout aktualisieren |
| DELETE | `/api/v1/layouts/{id}` | Layout löschen |
| GET | `/api/v1/layouts/{id}/variants` | Layout-Varianten |

### Images
| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/api/v1/images/upload` | Bild hochladen |
| GET | `/api/v1/images/{imageId}` | Bild abrufen |

### PDF
| Method | Endpoint | Beschreibung |
|--------|----------|--------------|
| POST | `/api/v1/pdf/generate` | PDF generieren |
| GET | `/api/v1/pdf/{id}/download` | PDF herunterladen |

## Entwicklung

### Tests ausführen
```bash
./gradlew test
```

### Build
```bash
./gradlew build
```

### Code-Stil
- Java 21 Features (Records, Pattern Matching)
- Spring Boot 3.4 Best Practices
- Property-Based Testing mit jqwik

## Projektstruktur

```
catForge-backend/
├── src/main/java/com/catalogforge/
│   ├── agent/           # Agent Framework (Pipelines, Steps)
│   ├── config/          # Spring Konfiguration
│   ├── controller/      # REST Controller
│   ├── exception/       # Exception Handling
│   ├── gemini/          # Gemini Client
│   ├── logging/         # LLM Logging
│   ├── model/           # Domain Models & DTOs
│   ├── pdf/             # PDF Generation
│   ├── service/         # Business Logic
│   ├── skills/          # Skills System
│   └── util/            # Utilities
├── src/main/resources/
│   ├── css/             # CSS Templates
│   ├── data/            # Produktdaten (JSON)
│   ├── prompts/         # Beispiel-Prompts
│   ├── skills/          # Skill Markdown Files
│   └── application.yml
├── scripts/             # Puppeteer PDF Generator
└── .devcontainer/       # Dev Container Config
```

## Skills-System

Skills sind modulare Markdown-Dateien für Prompt-Engineering:

- **Core**: Layout-Prinzipien, Typografie, Farben, Grid, Spacing
- **Styles**: Modern, Technical, Premium, Eco, Dynamic
- **Formats**: A4, A5, DL, A6, Square
- **Image-Analysis**: Farbextraktion, Stimmungsanalyse

## Lizenz

Proprietär - Alle Rechte vorbehalten.
