# Requirements Document

## Introduction

React-basiertes Frontend für die CatalogForge-Plattform – eine KI-gestützte Katalog- und Flyer-Erstellungsanwendung. Das Frontend unterstützt alle Backend-Funktionen und bietet ein professionelles Dashboard mit Pastellfarben-Design. Die Anwendung ermöglicht Benutzern, Produkte zu durchsuchen, Skills und Prompts zu erkunden, und über einen mehrstufigen Wizard KI-generierte Katalog-Layouts zu erstellen und als PDF zu exportieren.

## Glossary

- **Frontend**: Die React-basierte Benutzeroberfläche der CatalogForge-Anwendung
- **Backend**: Der Spring Boot REST API Server unter `http://localhost:8080`
- **Product**: Ein Daimler Truck Produkt mit Name, Beschreibung, Kategorie, Serie, Spezifikationen und Preis
- **Skill**: Eine Markdown-basierte Prompt-Engineering-Komponente für Layout-Generierung
- **Layout**: Ein KI-generiertes HTML/CSS-Dokument für Katalogseiten
- **Variant**: Eine alternative Version eines generierten Layouts
- **Wizard**: Der mehrstufige Prozess zur Katalog-Erstellung
- **PDF_Export**: Die Funktion zum Generieren druckfertiger PDFs aus Layouts
- **Print_Preset**: Vordefinierte Druckeinstellungen (DPI, Beschnitt, Schnittmarken)
- **API_Client**: Die Abstraktionsschicht für HTTP-Kommunikation mit dem Backend
- **Toast**: Eine temporäre Benachrichtigungskomponente für Feedback

## Requirements

### Requirement 1: Projekt-Setup und Konfiguration

**User Story:** Als Entwickler möchte ich ein vollständig konfiguriertes React-Projekt mit TypeScript, Vite und TailwindCSS, damit ich sofort mit der Entwicklung beginnen kann.

#### Acceptance Criteria

1. THE Frontend SHALL use React 18.x with TypeScript 5.x as the UI framework
2. THE Frontend SHALL use Vite 5.x as the build tool with hot module replacement
3. THE Frontend SHALL use TailwindCSS 3.x for styling with custom pastel color palette
4. THE Frontend SHALL configure path aliases using `@/` prefix for src directory imports
5. THE Frontend SHALL configure Vite proxy to forward `/api/*` requests to `http://localhost:8080`
6. THE Frontend SHALL include all required dependencies: React Router 6.x, TanStack Query 5.x, Zustand 4.x, Recharts 2.x, React Hook Form 7.x, Lucide React, React Markdown

### Requirement 2: Design-System und UI-Komponenten

**User Story:** Als Benutzer möchte ich eine konsistente, professionelle Benutzeroberfläche mit Pastellfarben, damit die Anwendung angenehm zu bedienen ist.

#### Acceptance Criteria

1. THE Frontend SHALL implement a pastel color palette with primary blue (#A8D5E5), green (#B5E5CF), purple (#D4B5E5), yellow (#F5E6A3), red (#E5B5B5), and orange (#E5CDB5)
2. THE Frontend SHALL use Inter font family as the primary typeface
3. THE Button component SHALL support variants: primary, secondary, ghost, danger with appropriate hover states
4. THE Card component SHALL have white background, subtle shadow, and 8px border-radius with hover effect
5. THE Badge component SHALL support color variants for different styles (modern=blue, technical=default, premium=purple, eco=green, dynamic=orange)
6. THE Input component SHALL have neutral-200 border with pastel-blue focus ring
7. THE Modal component SHALL support keyboard navigation (Escape to close) and click-outside-to-close
8. THE Spinner component SHALL provide visual loading feedback
9. THE Toast component SHALL display temporary notifications with German error messages

### Requirement 3: App-Layout und Navigation

**User Story:** Als Benutzer möchte ich eine übersichtliche Navigation mit Sidebar, damit ich schnell zwischen den verschiedenen Bereichen der Anwendung wechseln kann.

#### Acceptance Criteria

1. THE AppLayout SHALL include a fixed sidebar and main content area
2. THE Sidebar SHALL display the CatalogForge logo and navigation items: Dashboard, Skill Explorer, Prompt Explorer, Katalog erstellen
3. THE Sidebar SHALL highlight "Katalog erstellen" as the primary call-to-action
4. THE Sidebar SHALL display backend connection status (Online/Offline indicator)
5. WHEN viewport width is less than 768px THEN THE Sidebar SHALL collapse to a hamburger menu
6. WHEN viewport width is 768px or greater THEN THE Sidebar SHALL be fixed at 240px width
7. WHEN viewport width is 1280px or greater THEN THE Sidebar SHALL be fixed at 280px width
8. THE Header component SHALL display the current page title

### Requirement 4: API-Client und Datentypen

**User Story:** Als Entwickler möchte ich einen typsicheren API-Client, damit ich zuverlässig mit dem Backend kommunizieren kann.

#### Acceptance Criteria

1. THE API_Client SHALL use fetch API with configurable base URL from environment variable VITE_API_URL
2. THE API_Client SHALL automatically add Content-Type: application/json header to requests
3. WHEN an API request fails THEN THE API_Client SHALL throw an ApiError with status, error, and message
4. THE Frontend SHALL define TypeScript interfaces for all API data types: Product, LayoutOptions, TextToLayoutRequest, ImageToLayoutRequest, LayoutResponse, VariantResponse, Skill, ExamplePrompt, PdfGenerateRequest, PdfGenerateResponse, PrintPreset, ImageUploadResponse, ErrorResponse
5. THE productsApi SHALL implement methods: getAll, getById, getCategories, getSeries, search
6. THE layoutsApi SHALL implement methods: generateFromText, generateFromImage, getById, update, delete, getVariants
7. THE skillsApi SHALL implement methods: getAll, getCategories, getByCategory, getExamplePrompts, reload
8. THE pdfApi SHALL implement methods: generate, download, getPresets
9. THE imagesApi SHALL implement methods: upload (multipart), uploadBase64

### Requirement 5: React Query Hooks

**User Story:** Als Entwickler möchte ich React Query Hooks für Server-State-Management, damit Daten effizient gecacht und synchronisiert werden.

#### Acceptance Criteria

1. THE useProducts hook SHALL fetch products with optional category and series filters
2. THE useProduct hook SHALL fetch a single product by ID
3. THE useCategories hook SHALL fetch all product categories
4. THE useSeries hook SHALL fetch all product series
5. THE useProductSearch hook SHALL search products with minimum 2 character query
6. THE useSkills hook SHALL fetch all skills
7. THE useSkillCategories hook SHALL fetch skill categories
8. THE useSkillsByCategory hook SHALL fetch skills filtered by category
9. THE useExamplePrompts hook SHALL fetch example prompts
10. THE useLayout hook SHALL fetch a layout by ID
11. THE useGenerateFromText mutation SHALL generate layout from text prompt
12. THE useGenerateFromImage mutation SHALL generate layout from image
13. THE useUpdateLayout mutation SHALL update an existing layout
14. THE useDeleteLayout mutation SHALL delete a layout
15. THE usePrintPresets hook SHALL fetch available print presets
16. THE useGeneratePdf mutation SHALL generate a PDF from layout
17. THE useDownloadPdf mutation SHALL download a generated PDF
18. THE QueryClient SHALL configure 5 minute staleTime and 1 retry for queries

### Requirement 6: Dashboard-Seite

**User Story:** Als Benutzer möchte ich eine Dashboard-Übersicht mit Produktstatistiken und Visualisierungen, damit ich einen schnellen Überblick über alle Produkte erhalte.

#### Acceptance Criteria

1. WHEN the Dashboard page loads THEN THE Frontend SHALL fetch products, categories, and series from the API
2. THE Dashboard SHALL display StatCards showing: total products count, categories count, series count, electric vehicles count
3. THE Dashboard SHALL display a PieChart showing product distribution by category
4. THE Dashboard SHALL display a BarChart showing products per series
5. THE Dashboard SHALL display a BarChart showing price distribution
6. THE Dashboard SHALL display a sortable and filterable ProductTable with columns: Name, Kategorie, Serie, Preis
7. WHEN a user clicks on a StatCard THEN THE ProductTable SHALL filter accordingly
8. WHEN a user clicks on a chart segment THEN THE ProductTable SHALL filter by that segment
9. WHEN a user hovers over chart elements THEN THE Frontend SHALL display tooltips with details
10. WHEN a user clicks on a table row THEN THE Frontend SHALL display a product detail modal
11. THE Dashboard SHALL support search input with debounced filtering
12. THE Dashboard SHALL support category and series dropdown filters
13. THE Dashboard SHALL display all UI texts in German

### Requirement 7: Skill Explorer-Seite

**User Story:** Als Benutzer möchte ich Skills durchsuchen und verstehen, damit ich die verfügbaren Layout-Optionen kennenlernen kann.

#### Acceptance Criteria

1. WHEN the Skill Explorer page loads THEN THE Frontend SHALL fetch all skills and categories from the API
2. THE Skill Explorer SHALL display a CategoryFilter with radio buttons: Alle, Core, Styles, Formats
3. THE Skill Explorer SHALL display skills in a responsive grid layout
4. THE SkillCard SHALL display: skill name, category badge, priority badge
5. WHEN a user selects a category filter THEN THE skill grid SHALL show only skills of that category
6. WHEN a user clicks on a SkillCard THEN THE Frontend SHALL display a SkillDetailModal
7. THE SkillDetailModal SHALL display: skill name, category, priority, dependencies as clickable badges, and markdown-rendered content
8. WHEN a user clicks on a dependency badge THEN THE Frontend SHALL navigate to that skill's detail
9. THE Skill Explorer SHALL display all UI texts in German

### Requirement 8: Prompt Explorer-Seite

**User Story:** Als Benutzer möchte ich Beispiel-Prompts erkunden und als Vorlage nutzen, damit ich schneller mit der Katalog-Erstellung beginnen kann.

#### Acceptance Criteria

1. WHEN the Prompt Explorer page loads THEN THE Frontend SHALL fetch example prompts from the API
2. THE Prompt Explorer SHALL display style and format dropdown filters
3. THE Prompt Explorer SHALL display prompts in a responsive grid layout
4. THE PromptCard SHALL display: title, description preview, style badge, format badge, "Verwenden" button
5. WHEN a user applies filters THEN THE prompt grid SHALL show only matching prompts
6. WHEN a user clicks on a PromptCard THEN THE Frontend SHALL display a PromptDetailModal
7. THE PromptDetailModal SHALL display: title, full description, complete prompt text, options (format, style, variants, includeSpecs)
8. WHEN a user clicks "Im Wizard verwenden" THEN THE Frontend SHALL navigate to the Wizard with pre-filled data
9. THE Prompt Explorer SHALL display all UI texts in German

### Requirement 9: Wizard State Management

**User Story:** Als Entwickler möchte ich einen Zustand-Store für den Wizard, damit der Wizard-Zustand über alle Schritte hinweg konsistent verwaltet wird.

#### Acceptance Criteria

1. THE wizardStore SHALL track currentStep (1-4)
2. THE wizardStore SHALL track selectedProducts array
3. THE wizardStore SHALL track options: pageFormat, style, variantCount, includeSpecs, complexStrategy
4. THE wizardStore SHALL track prompt string (max 5000 characters)
5. THE wizardStore SHALL track referenceImage with base64 and mimeType
6. THE wizardStore SHALL track chatHistory array with messages
7. THE wizardStore SHALL track generatedLayout response
8. THE wizardStore SHALL track selectedVariantId
9. THE wizardStore SHALL track isGenerating and error states
10. THE wizardStore SHALL provide navigation methods: setStep, nextStep, prevStep
11. THE wizardStore SHALL provide product methods: addProduct, removeProduct, clearProducts
12. THE wizardStore SHALL provide a reset method to clear all state
13. THE wizardStore SHALL initialize chatHistory with a German welcome message

### Requirement 10: Katalog-Wizard Schritt 1 - Produktauswahl

**User Story:** Als Benutzer möchte ich Produkte für meinen Katalog auswählen, damit ich die gewünschten Produkte in mein Layout einbeziehen kann.

#### Acceptance Criteria

1. THE ProductSelector SHALL display a search input for product search
2. THE ProductSelector SHALL display category and series dropdown filters
3. THE ProductSelector SHALL display selected products count and list
4. WHEN a user searches THEN THE ProductSelector SHALL filter products with 300ms debounce
5. WHEN a user selects a product THEN THE product SHALL be added to selectedProducts
6. WHEN a user clicks "Entfernen" on a selected product THEN THE product SHALL be removed from selectedProducts
7. THE ProductSelector SHALL display available products in a grid with "Add" buttons
8. WHEN no products are selected THEN THE "Weiter" button SHALL be disabled
9. WHEN at least one product is selected THEN THE "Weiter" button SHALL be enabled
10. THE ProductSelector SHALL display all UI texts in German

### Requirement 11: Katalog-Wizard Schritt 2 - Layout-Optionen

**User Story:** Als Benutzer möchte ich Layout-Optionen konfigurieren, damit ich das Format und den Stil meines Katalogs bestimmen kann.

#### Acceptance Criteria

1. THE LayoutOptions SHALL display page format selection: A4, A5, DL, A6, SQUARE with visual indicators
2. THE LayoutOptions SHALL display format dimensions (e.g., "210×297mm" for A4)
3. THE LayoutOptions SHALL display style selection: Modern, Technical, Premium, Eco, Dynamic
4. THE LayoutOptions SHALL display a variant count slider (1-5) with current value display
5. THE LayoutOptions SHALL display a checkbox for "Technische Daten einbeziehen"
6. THE LayoutOptions SHALL display a checkbox for "Komplexe Strategie"
7. THE LayoutOptions SHALL default to: A4 format, modern style, 2 variants, includeSpecs=true, complexStrategy=false
8. THE LayoutOptions SHALL provide "Zurück" and "Weiter" navigation buttons
9. THE LayoutOptions SHALL display all UI texts in German

### Requirement 12: Katalog-Wizard Schritt 3 - Prompt-Eingabe

**User Story:** Als Benutzer möchte ich meinen Katalog beschreiben und optional ein Referenzbild hochladen, damit die KI ein passendes Layout generieren kann.

#### Acceptance Criteria

1. THE PromptInput SHALL display a chat history showing previous messages
2. THE PromptInput SHALL display a textarea for prompt input with character counter (max 5000)
3. THE PromptInput SHALL display example prompt quick-select buttons
4. THE ImageUpload SHALL support drag-and-drop file upload
5. THE ImageUpload SHALL support click-to-select file upload
6. THE ImageUpload SHALL accept only JPG and PNG files up to 10MB
7. WHEN a file exceeds 10MB THEN THE Frontend SHALL display an error toast
8. WHEN an image is uploaded THEN THE ImageUpload SHALL display a preview
9. THE ImageUpload SHALL provide a button to remove the uploaded image
10. WHEN prompt is empty THEN THE "Generieren" button SHALL be disabled
11. WHEN prompt is not empty THEN THE "Generieren" button SHALL be enabled
12. THE PromptInput SHALL provide "Zurück" and "Generieren" navigation buttons
13. THE PromptInput SHALL display all UI texts in German

### Requirement 13: Katalog-Wizard Schritt 4 - Ergebnis und Feedback-Loop

**User Story:** Als Benutzer möchte ich die generierten Layout-Varianten sehen und Feedback geben können, damit ich das beste Ergebnis erhalte.

#### Acceptance Criteria

1. WHEN the user clicks "Generieren" THEN THE Frontend SHALL call the appropriate API (text or image based)
2. WHILE generating THEN THE Frontend SHALL display a loading spinner with "Generiere Layout..." message
3. IF generation fails THEN THE Frontend SHALL display an error message and allow retry
4. WHEN generation succeeds THEN THE Frontend SHALL display variant tabs for each generated variant
5. THE LayoutPreview SHALL render the HTML/CSS in an isolated iframe or shadow DOM
6. THE LayoutPreview SHALL display zoom controls: 50%, 75%, 100%, 150%
7. THE Frontend SHALL display a chat interface for feedback
8. WHEN a user submits feedback THEN THE Frontend SHALL add the message to chat history
9. THE Frontend SHALL provide a "Neu generieren" button to regenerate with updated prompt
10. THE Frontend SHALL provide "Zurück" and "Diese Variante verwenden" navigation buttons
11. WHEN user clicks "Diese Variante verwenden" THEN THE Frontend SHALL navigate to preview page with layoutId
12. THE Frontend SHALL display generation time in the chat response
13. THE Frontend SHALL display all UI texts in German

### Requirement 14: Katalog-Vorschau und Export-Seite

**User Story:** Als Benutzer möchte ich mein generiertes Layout ansehen, bearbeiten und als PDF exportieren, damit ich ein druckfertiges Dokument erhalte.

#### Acceptance Criteria

1. WHEN the Preview page loads THEN THE Frontend SHALL fetch the layout by ID from URL parameter
2. THE Preview page SHALL display the layout in a LayoutPreview component with zoom controls
3. THE Preview page SHALL display a VariantSelector with radio buttons for each variant
4. THE Preview page SHALL display metadata: format, generation time, LLM call count, skills used
5. THE Preview page SHALL display a collapsible CodeEditor with HTML and CSS tabs
6. WHEN a user edits code THEN THE LayoutPreview SHALL update in real-time
7. WHEN a user clicks "Speichern" THEN THE Frontend SHALL call the update layout API
8. THE Preview page SHALL display a PresetSelector dropdown with print presets
9. THE Preview page SHALL display preset details: DPI, bleed, crop marks, description
10. WHEN a user clicks "PDF generieren und herunterladen" THEN THE Frontend SHALL call generate PDF API and trigger download
11. WHILE PDF is generating THEN THE Frontend SHALL display a loading state
12. THE Preview page SHALL provide a "Neuer Katalog" button to navigate back to wizard
13. THE Preview page SHALL provide a delete button to remove the layout
14. WHEN delete is clicked THEN THE Frontend SHALL confirm and delete the layout
15. THE Preview page SHALL display all UI texts in German

### Requirement 15: Error Handling

**User Story:** Als Benutzer möchte ich verständliche Fehlermeldungen erhalten, damit ich weiß, was schiefgelaufen ist und wie ich fortfahren kann.

#### Acceptance Criteria

1. WHEN a 400 error occurs THEN THE Frontend SHALL display "Ungültige Eingabe. Bitte überprüfen Sie Ihre Daten."
2. WHEN a 404 error occurs THEN THE Frontend SHALL display "Die angeforderte Ressource wurde nicht gefunden."
3. WHEN a 413 error occurs THEN THE Frontend SHALL display "Die Datei ist zu groß. Maximale Größe: 10MB."
4. WHEN a 502 error occurs THEN THE Frontend SHALL display "Der KI-Service ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut."
5. WHEN a 500 error occurs THEN THE Frontend SHALL display "Ein unerwarteter Fehler ist aufgetreten."
6. THE Toast component SHALL auto-dismiss after 5 seconds
7. THE Toast component SHALL support manual dismissal

### Requirement 16: Responsive Design

**User Story:** Als Benutzer möchte ich die Anwendung auf verschiedenen Geräten nutzen können, damit ich flexibel arbeiten kann.

#### Acceptance Criteria

1. THE Frontend SHALL use Tailwind breakpoints: sm (640px), md (768px), lg (1024px), xl (1280px), 2xl (1536px)
2. WHEN viewport is less than 768px THEN THE sidebar SHALL collapse to hamburger menu
3. WHEN viewport is less than 768px THEN THE content SHALL use full width
4. THE product grid SHALL adjust columns based on viewport: 1 column on mobile, 2 on tablet, 3-4 on desktop
5. THE chart components SHALL resize responsively
6. THE wizard steps SHALL stack vertically on mobile

### Requirement 17: Accessibility

**User Story:** Als Benutzer mit Einschränkungen möchte ich die Anwendung barrierefrei nutzen können, damit ich alle Funktionen erreichen kann.

#### Acceptance Criteria

1. THE Frontend SHALL support keyboard navigation for all interactive elements
2. THE Frontend SHALL display visible focus rings using pastel-blue color
3. THE Frontend SHALL include ARIA labels for icons and interactive elements
4. THE Frontend SHALL maintain minimum 4.5:1 color contrast ratio for text
5. THE Frontend SHALL use semantic HTML elements
6. THE Frontend SHALL use aria-live regions for toast notifications
7. THE Frontend SHALL respect prefers-reduced-motion user preference

### Requirement 18: Performance-Optimierungen

**User Story:** Als Benutzer möchte ich eine schnelle und reaktionsschnelle Anwendung, damit ich effizient arbeiten kann.

#### Acceptance Criteria

1. THE Frontend SHALL implement code splitting with lazy loading for page components
2. THE Frontend SHALL configure React Query with 5 minute staleTime for caching
3. THE Frontend SHALL implement lazy loading for product images
4. THE Frontend SHALL use tree-shaking for Lucide icons (import only used icons)
5. THE Frontend SHALL implement 300ms debounce for search inputs
