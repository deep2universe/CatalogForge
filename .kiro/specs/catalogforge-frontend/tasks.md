# Implementation Plan: CatalogForge Frontend

## Overview

Dieser Implementierungsplan beschreibt die schrittweise Entwicklung des CatalogForge React Frontends. Die Tasks sind so strukturiert, dass jeder Schritt auf dem vorherigen aufbaut und das Projekt inkrementell wächst. Property-Based Tests werden direkt nach der Implementierung der entsprechenden Komponenten geschrieben.

## Git Workflow

### Commit-Konventionen

Verwende Conventional Commits für alle Commits:

| Prefix | Verwendung |
|--------|------------|
| `feat:` | Neue Features und Funktionalitäten |
| `fix:` | Bugfixes und Korrekturen |
| `test:` | Tests hinzufügen oder ändern |
| `chore:` | Build-Konfiguration, Dependencies, Setup |
| `docs:` | Dokumentation |
| `refactor:` | Code-Refactoring ohne Funktionsänderung |
| `style:` | Formatierung, Whitespace (kein Code-Change) |

### Commit-Regeln

1. **Nach jedem abgeschlossenen Subtask committen**
2. **Commit-Message auf Englisch**, Scope in Klammern
3. **Atomare Commits** - ein logischer Change pro Commit
4. **Tests mit zugehörigem Code committen** (nicht separat)

### Beispiele

```bash
# Setup
git commit -m "chore(setup): initialize Vite React TypeScript project"
git commit -m "chore(tailwind): configure TailwindCSS with pastel color palette"

# Features
git commit -m "feat(api): add TypeScript interfaces and API client"
git commit -m "feat(ui): add Button, Card, Badge components"
git commit -m "feat(dashboard): implement Dashboard page with charts"

# Tests
git commit -m "test(api): add property tests for error handling"
git commit -m "test(ui): add property tests for variant mapping"

# Fixes
git commit -m "fix(wizard): correct step navigation logic"
```

## Tasks

- [ ] 1. Projekt-Setup und Konfiguration
  - [ ] 1.1 Vite React TypeScript Projekt initialisieren
    - Erstelle `catForge-frontend/` Verzeichnis
    - Initialisiere mit `npm create vite@latest` (React + TypeScript)
    - Konfiguriere `package.json` mit allen Dependencies
    - _Requirements: 1.1, 1.2, 1.6_
    - **Commit:** `chore(setup): initialize Vite React TypeScript project`

  - [ ] 1.2 TailwindCSS und Design-System konfigurieren
    - Installiere und konfiguriere TailwindCSS
    - Erstelle `tailwind.config.js` mit Pastellfarben-Palette
    - Erstelle `src/styles/globals.css` mit CSS-Variablen
    - Konfiguriere Inter Font
    - _Requirements: 1.3, 2.1, 2.2_
    - **Commit:** `chore(tailwind): configure TailwindCSS with design system`

  - [ ] 1.3 Vite und TypeScript konfigurieren
    - Konfiguriere `vite.config.ts` mit Proxy und Path-Aliases
    - Konfiguriere `tsconfig.json` mit `@/` Alias
    - Erstelle `.env.example` mit VITE_API_URL
    - _Requirements: 1.4, 1.5_
    - **Commit:** `chore(config): configure Vite proxy and TypeScript paths`

  - [ ] 1.4 Testing-Framework einrichten
    - Installiere Vitest, React Testing Library, fast-check
    - Konfiguriere `vitest.config.ts`
    - Erstelle Test-Setup-Datei
    - _Requirements: Testing Strategy_
    - **Commit:** `chore(test): setup Vitest with React Testing Library`

- [ ] 2. API Layer implementieren
  - [ ] 2.1 TypeScript Interfaces und API Client erstellen
    - Erstelle `src/api/types.ts` mit allen Interfaces
    - Erstelle `src/api/client.ts` mit apiClient Funktion
    - Erstelle `src/api/errors.ts` mit ApiError Klasse
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
    - **Commit:** `feat(api): add TypeScript interfaces and API client`

  - [ ] 2.2 Property Test für API Client Error Handling
    - **Property 3: API Client Error Handling**
    - **Validates: Requirements 4.3**
    - **Commit:** `test(api): add property tests for error handling`

  - [ ] 2.3 API Module implementieren
    - Erstelle `src/api/products.ts`
    - Erstelle `src/api/layouts.ts`
    - Erstelle `src/api/skills.ts`
    - Erstelle `src/api/pdf.ts`
    - Erstelle `src/api/images.ts`
    - _Requirements: 4.5, 4.6, 4.7, 4.8, 4.9_
    - **Commit:** `feat(api): add API modules for products, layouts, skills, pdf, images`

- [ ] 3. Checkpoint - API Layer
  - Stelle sicher, dass alle API-Typen korrekt definiert sind
  - Führe TypeScript-Kompilierung durch
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(api): resolve type issues from checkpoint review`

- [ ] 4. UI-Komponenten Bibliothek
  - [ ] 4.1 Basis UI-Komponenten erstellen
    - Erstelle `src/components/ui/Button.tsx`
    - Erstelle `src/components/ui/Card.tsx`
    - Erstelle `src/components/ui/Badge.tsx`
    - Erstelle `src/components/ui/Input.tsx`
    - Erstelle `src/components/ui/Select.tsx`
    - _Requirements: 2.3, 2.4, 2.5, 2.6_
    - **Commit:** `feat(ui): add Button, Card, Badge, Input, Select components`

  - [ ] 4.2 Property Test für UI Component Variant Mapping
    - **Property 1: UI Component Variant Mapping**
    - **Validates: Requirements 2.3, 2.5, 2.9**
    - **Commit:** `test(ui): add property tests for variant mapping`

  - [ ] 4.3 Property Test für Style to Badge Color Mapping
    - **Property 10: Style to Badge Color Mapping**
    - **Validates: Requirements 2.5**
    - **Commit:** `test(ui): add property tests for badge color mapping`

  - [ ] 4.4 Erweiterte UI-Komponenten erstellen
    - Erstelle `src/components/ui/Modal.tsx`
    - Erstelle `src/components/ui/Tabs.tsx`
    - Erstelle `src/components/ui/Slider.tsx`
    - Erstelle `src/components/ui/Toggle.tsx`
    - Erstelle `src/components/ui/Spinner.tsx`
    - Erstelle `src/components/ui/Toast.tsx`
    - _Requirements: 2.7, 2.8, 2.9_
    - **Commit:** `feat(ui): add Modal, Tabs, Slider, Toggle, Spinner, Toast components`

  - [ ] 4.5 Property Test für Error Message Mapping
    - **Property 9: Error Message Mapping**
    - **Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**
    - **Commit:** `test(ui): add property tests for error message mapping`

- [ ] 5. Checkpoint - UI-Komponenten
  - Stelle sicher, dass alle UI-Komponenten rendern
  - Führe Property Tests aus
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(ui): resolve issues from checkpoint review`

- [ ] 6. Layout-Komponenten und Navigation
  - [ ] 6.1 App Layout erstellen
    - Erstelle `src/components/layout/AppLayout.tsx`
    - Erstelle `src/components/layout/Sidebar.tsx`
    - Erstelle `src/components/layout/Header.tsx`
    - Erstelle `src/components/layout/PageContainer.tsx`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.8_
    - **Commit:** `feat(layout): add AppLayout, Sidebar, Header, PageContainer`

  - [ ] 6.2 Property Test für Responsive Sidebar Behavior
    - **Property 2: Responsive Sidebar Behavior**
    - **Validates: Requirements 3.5, 3.6, 3.7**
    - **Commit:** `test(layout): add property tests for responsive sidebar`

  - [ ] 6.3 React Router Setup
    - Erstelle `src/App.tsx` mit Router-Konfiguration
    - Erstelle `src/main.tsx` mit QueryClient Provider
    - Konfiguriere alle Routes
    - _Requirements: Routing (Section 9)_
    - **Commit:** `feat(router): configure React Router with all routes`

- [ ] 7. React Query Hooks
  - [ ] 7.1 Product Hooks erstellen
    - Erstelle `src/hooks/useProducts.ts`
    - Implementiere useProducts, useProduct, useCategories, useSeries, useProductSearch
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
    - **Commit:** `feat(hooks): add product hooks with React Query`

  - [ ] 7.2 Skills und Layout Hooks erstellen
    - Erstelle `src/hooks/useSkills.ts`
    - Erstelle `src/hooks/useLayouts.ts`
    - _Requirements: 5.6, 5.7, 5.8, 5.9, 5.10, 5.11, 5.12, 5.13, 5.14_
    - **Commit:** `feat(hooks): add skills and layout hooks`

  - [ ] 7.3 PDF und Image Hooks erstellen
    - Erstelle `src/hooks/usePdf.ts`
    - Erstelle `src/hooks/useImageUpload.ts`
    - _Requirements: 5.15, 5.16, 5.17_
    - **Commit:** `feat(hooks): add PDF and image upload hooks`

- [ ] 8. Utility Functions
  - [ ] 8.1 Formatter und Validator Funktionen erstellen
    - Erstelle `src/utils/formatters.ts` (Preis, Datum, etc.)
    - Erstelle `src/utils/validators.ts` (Prompt-Länge, Datei-Größe)
    - Erstelle `src/utils/filters.ts` (Produkt-Filter, Skill-Filter)
    - _Requirements: 6.6, 6.11, 6.12, 7.5, 8.5_
    - **Commit:** `feat(utils): add formatters, validators, and filter utilities`

  - [ ] 8.2 Property Test für Product Filtering and Sorting
    - **Property 5: Product Filtering and Sorting**
    - **Validates: Requirements 6.6, 6.11, 6.12**
    - **Commit:** `test(utils): add property tests for product filtering`

  - [ ] 8.3 Property Test für Page Format Dimensions
    - **Property 11: Page Format Dimensions**
    - **Validates: Requirements 11.2**
    - **Commit:** `test(utils): add property tests for page format dimensions`

- [ ] 9. Checkpoint - Core Infrastructure
  - Stelle sicher, dass alle Hooks und Utilities funktionieren
  - Führe alle Property Tests aus
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(core): resolve issues from infrastructure checkpoint`

- [ ] 10. Chart-Komponenten
  - [ ] 10.1 Chart-Komponenten erstellen
    - Erstelle `src/components/charts/StatCard.tsx`
    - Erstelle `src/components/charts/PieChart.tsx`
    - Erstelle `src/components/charts/BarChart.tsx`
    - _Requirements: 6.2, 6.3, 6.4, 6.5_
    - **Commit:** `feat(charts): add StatCard, PieChart, BarChart components`

  - [ ] 10.2 Property Test für Dashboard Data Aggregation
    - **Property 4: Dashboard Data Aggregation**
    - **Validates: Requirements 6.2, 6.3, 6.4, 6.5**
    - **Commit:** `test(charts): add property tests for data aggregation`

- [ ] 11. Dashboard-Seite
  - [ ] 11.1 Dashboard Feature-Komponenten erstellen
    - Erstelle `src/components/features/products/ProductCard.tsx`
    - Erstelle `src/components/features/products/ProductGrid.tsx`
    - Erstelle `src/components/features/products/ProductSearch.tsx`
    - Erstelle `src/components/features/products/ProductFilter.tsx`
    - _Requirements: 6.6, 6.7, 6.8, 6.11, 6.12_
    - **Commit:** `feat(products): add ProductCard, ProductGrid, ProductSearch, ProductFilter`

  - [ ] 11.2 Dashboard Page implementieren
    - Erstelle `src/pages/Dashboard/DashboardPage.tsx`
    - Integriere StatCards, Charts, ProductTable
    - Implementiere Filter-Interaktionen
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10, 6.13_
    - **Commit:** `feat(dashboard): implement Dashboard page with statistics and products`

- [ ] 12. Skill Explorer-Seite
  - [ ] 12.1 Skill Feature-Komponenten erstellen
    - Erstelle `src/components/features/skills/SkillCard.tsx`
    - Erstelle `src/components/features/skills/SkillDetail.tsx`
    - Erstelle `src/components/features/skills/CategoryFilter.tsx`
    - _Requirements: 7.2, 7.3, 7.4, 7.6, 7.7_
    - **Commit:** `feat(skills): add SkillCard, SkillDetail, CategoryFilter components`

  - [ ] 12.2 Property Test für Skill Display and Filtering
    - **Property 6: Skill Display and Filtering**
    - **Validates: Requirements 7.4, 7.5, 7.7**
    - **Commit:** `test(skills): add property tests for skill filtering`

  - [ ] 12.3 Skill Explorer Page implementieren
    - Erstelle `src/pages/SkillExplorer/SkillExplorerPage.tsx`
    - Integriere CategoryFilter, SkillGrid, SkillDetailModal
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9_
    - **Commit:** `feat(skills): implement Skill Explorer page`

- [ ] 13. Prompt Explorer-Seite
  - [ ] 13.1 Prompt Feature-Komponenten erstellen
    - Erstelle `src/components/features/prompts/PromptCard.tsx`
    - Erstelle `src/components/features/prompts/PromptDetail.tsx`
    - _Requirements: 8.2, 8.3, 8.4, 8.6, 8.7_
    - **Commit:** `feat(prompts): add PromptCard, PromptDetail components`

  - [ ] 13.2 Property Test für Prompt Display and Filtering
    - **Property 7: Prompt Display and Filtering**
    - **Validates: Requirements 8.4, 8.5, 8.7**
    - **Commit:** `test(prompts): add property tests for prompt filtering`

  - [ ] 13.3 Prompt Explorer Page implementieren
    - Erstelle `src/pages/PromptExplorer/PromptExplorerPage.tsx`
    - Integriere Filter, PromptGrid, PromptDetailModal
    - Implementiere "Im Wizard verwenden" Navigation
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9_
    - **Commit:** `feat(prompts): implement Prompt Explorer page`

- [ ] 14. Checkpoint - Explorer Pages
  - Stelle sicher, dass Dashboard, Skill Explorer und Prompt Explorer funktionieren
  - Führe alle Property Tests aus
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(pages): resolve issues from explorer pages checkpoint`

- [ ] 15. Wizard State Management
  - [ ] 15.1 Wizard Store implementieren
    - Erstelle `src/store/wizardStore.ts`
    - Implementiere alle State-Felder und Actions
    - Implementiere reset() Funktion
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 9.10, 9.11, 9.12, 9.13_
    - **Commit:** `feat(store): implement Wizard store with Zustand`

  - [ ] 15.2 Property Test für Wizard Store State Management
    - **Property 8: Wizard Store State Management**
    - **Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.6, 9.10, 9.11, 9.12**
    - **Commit:** `test(store): add property tests for wizard state management`

- [ ] 16. Wizard Schritt 1 - Produktauswahl
  - [ ] 16.1 Wizard Step 1 Komponenten erstellen
    - Erstelle `src/components/features/wizard/StepIndicator.tsx`
    - Erstelle `src/components/features/wizard/ProductSelector.tsx`
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9, 10.10_
    - **Commit:** `feat(wizard): add Step 1 - ProductSelector with StepIndicator`

- [ ] 17. Wizard Schritt 2 - Layout-Optionen
  - [ ] 17.1 Wizard Step 2 Komponenten erstellen
    - Erstelle `src/components/features/wizard/LayoutOptions.tsx`
    - Implementiere Format-Auswahl, Style-Auswahl, Varianten-Slider
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8, 11.9_
    - **Commit:** `feat(wizard): add Step 2 - LayoutOptions with format and style selection`

- [ ] 18. Wizard Schritt 3 - Prompt-Eingabe
  - [ ] 18.1 Wizard Step 3 Komponenten erstellen
    - Erstelle `src/components/features/wizard/PromptInput.tsx`
    - Erstelle `src/components/features/wizard/ImageUpload.tsx`
    - Erstelle `src/components/features/wizard/ChatInterface.tsx`
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9, 12.10, 12.11, 12.12, 12.13_
    - **Commit:** `feat(wizard): add Step 3 - PromptInput, ImageUpload, ChatInterface`

  - [ ] 18.2 Property Test für Character Counter Validation
    - **Property 12: Character Counter Validation**
    - **Validates: Requirements 12.2, 12.10, 12.11**
    - **Commit:** `test(wizard): add property tests for character counter validation`

- [ ] 19. Wizard Schritt 4 - Ergebnis
  - [ ] 19.1 Wizard Step 4 Komponenten erstellen
    - Erstelle `src/components/features/preview/LayoutPreview.tsx`
    - Erstelle `src/components/features/preview/VariantSelector.tsx`
    - Implementiere Zoom-Controls
    - _Requirements: 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10, 13.11_
    - **Commit:** `feat(wizard): add Step 4 - LayoutPreview with VariantSelector`

  - [ ] 19.2 Catalog Wizard Page implementieren
    - Erstelle `src/pages/CatalogWizard/CatalogWizardPage.tsx`
    - Integriere alle 4 Wizard-Schritte
    - Implementiere Generierungs-Flow
    - _Requirements: 13.1, 13.2, 13.3, 13.12, 13.13_
    - **Commit:** `feat(wizard): implement CatalogWizard page with full generation flow`

- [ ] 20. Checkpoint - Wizard
  - Stelle sicher, dass der komplette Wizard-Flow funktioniert
  - Führe alle Property Tests aus
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(wizard): resolve issues from wizard checkpoint`

- [ ] 21. Katalog-Vorschau und Export
  - [ ] 21.1 Preview Feature-Komponenten erstellen
    - Erstelle `src/components/features/preview/CodeEditor.tsx`
    - Erstelle `src/components/features/preview/PdfExport.tsx`
    - _Requirements: 14.5, 14.6, 14.7, 14.8, 14.9, 14.10, 14.11_
    - **Commit:** `feat(preview): add CodeEditor and PdfExport components`

  - [ ] 21.2 Catalog Preview Page implementieren
    - Erstelle `src/pages/CatalogPreview/CatalogPreviewPage.tsx`
    - Integriere LayoutPreview, VariantSelector, CodeEditor, PdfExport
    - Implementiere Delete-Funktion
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.12, 14.13, 14.14, 14.15_
    - **Commit:** `feat(preview): implement CatalogPreview page with export functionality`

- [ ] 22. Error Handling und Toast System
  - [ ] 22.1 Globales Error Handling implementieren
    - Erstelle Error Boundary Komponente
    - Implementiere Toast-Context und Provider
    - Integriere Error Handling in API-Aufrufe
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7_
    - **Commit:** `feat(error): implement ErrorBoundary and Toast notification system`

- [ ] 23. Responsive Design und Accessibility
  - [ ] 23.1 Responsive Anpassungen
    - Implementiere mobile Sidebar (Hamburger-Menü)
    - Passe Grids für verschiedene Viewports an
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6_
    - **Commit:** `feat(responsive): add mobile sidebar and responsive grid layouts`

  - [ ] 23.2 Accessibility Verbesserungen
    - Füge ARIA-Labels hinzu
    - Implementiere Keyboard-Navigation
    - Überprüfe Farbkontraste
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_
    - **Commit:** `feat(a11y): add ARIA labels, keyboard navigation, contrast fixes`

- [ ] 24. Performance-Optimierungen
  - [ ] 24.1 Code Splitting und Lazy Loading
    - Implementiere Lazy Loading für Pages
    - Optimiere Bundle-Größe
    - Implementiere Debouncing für Suche
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5_
    - **Commit:** `perf(bundle): add lazy loading, code splitting, search debouncing`

- [ ] 25. Final Checkpoint
  - Führe alle Tests aus (Unit + Property)
  - Überprüfe TypeScript-Kompilierung
  - Teste alle User Flows manuell
  - Frage den Benutzer bei Unklarheiten
  - **Commit (falls Fixes nötig):** `fix(final): resolve issues from final checkpoint`
  - **Final Commit:** `chore(release): complete CatalogForge frontend implementation`

## Notes

- Alle Tasks sind erforderlich für eine vollständige Implementierung
- Jeder Task referenziert spezifische Requirements für Nachverfolgbarkeit
- Checkpoints dienen der inkrementellen Validierung
- Property Tests validieren universelle Korrektheitseigenschaften
- Unit Tests validieren spezifische Beispiele und Edge Cases
