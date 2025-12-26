---
inclusion: fileMatch
fileMatchPattern: ['catForge-backend/**/*.java', 'catForge-backend/**/*.gradle.kts', 'catForge-backend/**/*.yml', 'catForge-frontend/**/*.ts', 'catForge-frontend/**/*.tsx']
---

# Tech Stack

## Frontend (`catForge-frontend/`)

### Runtime & Build

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.2 | UI library |
| TypeScript | 5.3 | Type safety |
| Vite | 5.1 | Build tool, dev server |
| Tailwind CSS | 3.4 | Utility-first styling |

### Dependencies

**Core:**
- `react-router-dom` 6.22 - Client-side routing
- `@tanstack/react-query` 5.24 - Server state management
- `zustand` 4.5 - Client state management
- `react-hook-form` 7.50 - Form handling

**UI:**
- `lucide-react` - Icon library
- `recharts` 2.12 - Charts and visualizations
- `react-markdown` 9.0 - Markdown rendering
- `clsx` + `tailwind-merge` - Class name utilities

### Testing Stack

| Library | Version | Use Case |
|---------|---------|----------|
| Vitest | 1.3 | Test runner |
| Testing Library | 14.2 | Component testing |
| fast-check | 3.15 | Property-based testing |
| jsdom | 24.0 | DOM environment |

### Commands

```bash
npm run dev          # Start dev server (port 3000)
npm run build        # Production build
npm run test         # Run tests (single run)
npm run test:watch   # Run tests in watch mode
npm run test:coverage # Coverage report
npm run lint         # ESLint check
```

### Configuration

- Dev server proxies `/api` to `http://localhost:8080` (backend)
- Path alias: `@/*` → `./src/*`
- Strict TypeScript with `noUnusedLocals`, `noUnusedParameters`

### Code Patterns

**Use React 18 features:**
- Functional components with hooks
- Suspense for loading states (with React Query)
- Error boundaries for error handling

**TypeScript conventions:**
- Interfaces for object shapes, types for unions
- Strict null checks enabled
- No `any` types

**Component patterns:**
- Props destructuring with TypeScript interfaces
- Custom hooks for reusable logic
- Composition over inheritance

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
