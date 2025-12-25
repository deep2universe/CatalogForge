# Design Document: CatalogForge Frontend

## Overview

Das CatalogForge Frontend ist eine React-basierte Single-Page-Application (SPA), die als Benutzeroberfläche für die KI-gestützte Katalog-Erstellung dient. Die Architektur folgt modernen React-Patterns mit klarer Trennung zwischen Präsentation, State-Management und API-Kommunikation.

### Kernprinzipien

- **Component-Based Architecture**: Wiederverwendbare UI-Komponenten mit klarer Verantwortlichkeit
- **Type Safety**: Vollständige TypeScript-Typisierung für alle Datenstrukturen und API-Aufrufe
- **Server State vs Client State**: React Query für Server-State, Zustand für Client-State
- **Responsive Design**: Mobile-first Ansatz mit TailwindCSS
- **Accessibility First**: WCAG 2.1 AA Konformität

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser                                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    React Application                      │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │                    Pages Layer                       │ │   │
│  │  │  Dashboard │ SkillExplorer │ PromptExplorer │ Wizard │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │               Components Layer                       │ │   │
│  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │ │   │
│  │  │  │   UI    │ │ Charts  │ │ Layout  │ │Features │   │ │   │
│  │  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │                  State Layer                         │ │   │
│  │  │  ┌───────────────┐  ┌───────────────┐               │ │   │
│  │  │  │ React Query   │  │   Zustand     │               │ │   │
│  │  │  │ (Server State)│  │(Client State) │               │ │   │
│  │  │  └───────────────┘  └───────────────┘               │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │                   API Layer                          │ │   │
│  │  │  apiClient │ productsApi │ layoutsApi │ skillsApi   │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                    HTTP (fetch API)                              │
├─────────────────────────────────────────────────────────────────┤
│                 Backend API (localhost:8080)                     │
└─────────────────────────────────────────────────────────────────┘
```

### Datenfluss

```
User Interaction → Component → Hook → API Client → Backend
                                ↓
                         React Query Cache
                                ↓
                    Component Re-render with Data
```

## Components and Interfaces

### API Layer

```typescript
// src/api/client.ts
interface ApiClientConfig {
  baseUrl: string;
  defaultHeaders: Record<string, string>;
}

async function apiClient<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T>;

class ApiError extends Error {
  constructor(public response: ErrorResponse);
  get status(): number;
  get isNotFound(): boolean;
  get isValidationError(): boolean;
  get isServerError(): boolean;
}
```

### Products API

```typescript
// src/api/products.ts
interface ProductsApi {
  getAll(category?: string, series?: string): Promise<Product[]>;
  getById(id: number): Promise<Product>;
  getCategories(): Promise<string[]>;
  getSeries(): Promise<string[]>;
  search(query: string): Promise<Product[]>;
}
```

### Layouts API

```typescript
// src/api/layouts.ts
interface LayoutsApi {
  generateFromText(request: TextToLayoutRequest): Promise<LayoutResponse>;
  generateFromImage(request: ImageToLayoutRequest): Promise<LayoutResponse>;
  getById(id: string): Promise<LayoutResponse>;
  update(id: string, layout: Partial<LayoutResponse>): Promise<LayoutResponse>;
  delete(id: string): Promise<void>;
  getVariants(id: string): Promise<VariantResponse[]>;
}
```

### Skills API

```typescript
// src/api/skills.ts
interface SkillsApi {
  getAll(): Promise<Skill[]>;
  getCategories(): Promise<string[]>;
  getByCategory(category: string): Promise<Skill[]>;
  getExamplePrompts(): Promise<ExamplePrompt[]>;
  reload(): Promise<void>;
}
```

### PDF API

```typescript
// src/api/pdf.ts
interface PdfApi {
  generate(request: PdfGenerateRequest): Promise<PdfGenerateResponse>;
  download(id: string): Promise<Blob>;
  getPresets(): Promise<PrintPreset[]>;
}
```

### Images API

```typescript
// src/api/images.ts
interface ImagesApi {
  upload(file: File): Promise<ImageUploadResponse>;
  uploadBase64(base64Data: string, mimeType: string, filename?: string): Promise<ImageUploadResponse>;
}
```

### UI Components

```typescript
// src/components/ui/Button.tsx
interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  loading?: boolean;
  children: React.ReactNode;
  onClick?: () => void;
}

// src/components/ui/Card.tsx
interface CardProps {
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
}

// src/components/ui/Badge.tsx
interface BadgeProps {
  variant?: 'default' | 'blue' | 'green' | 'purple' | 'yellow' | 'orange';
  children: React.ReactNode;
}

// src/components/ui/Input.tsx
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

// src/components/ui/Select.tsx
interface SelectProps {
  options: { value: string; label: string }[];
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  label?: string;
}

// src/components/ui/Modal.tsx
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

// src/components/ui/Toast.tsx
interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  onDismiss: () => void;
}

// src/components/ui/Spinner.tsx
interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

// src/components/ui/Tabs.tsx
interface TabsProps {
  tabs: { id: string; label: string; content: React.ReactNode }[];
  activeTab: string;
  onChange: (tabId: string) => void;
}

// src/components/ui/Slider.tsx
interface SliderProps {
  min: number;
  max: number;
  value: number;
  onChange: (value: number) => void;
  label?: string;
}

// src/components/ui/Toggle.tsx
interface ToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label?: string;
}
```

### Chart Components

```typescript
// src/components/charts/StatCard.tsx
interface StatCardProps {
  title: string;
  value: number | string;
  icon: React.ReactNode;
  trend?: { value: number; isPositive: boolean };
  onClick?: () => void;
}

// src/components/charts/PieChart.tsx
interface PieChartProps {
  data: { name: string; value: number; color: string }[];
  title: string;
  onSegmentClick?: (name: string) => void;
}

// src/components/charts/BarChart.tsx
interface BarChartProps {
  data: { name: string; value: number }[];
  title: string;
  xAxisLabel?: string;
  yAxisLabel?: string;
  onBarClick?: (name: string) => void;
}
```

### Layout Components

```typescript
// src/components/layout/AppLayout.tsx
interface AppLayoutProps {
  children: React.ReactNode;
}

// src/components/layout/Sidebar.tsx
interface SidebarProps {
  isCollapsed: boolean;
  onToggle: () => void;
}

// src/components/layout/Header.tsx
interface HeaderProps {
  title: string;
  actions?: React.ReactNode;
}

// src/components/layout/PageContainer.tsx
interface PageContainerProps {
  children: React.ReactNode;
  className?: string;
}
```

### Feature Components

```typescript
// Products
interface ProductCardProps {
  product: Product;
  onSelect?: () => void;
  isSelected?: boolean;
  showAddButton?: boolean;
}

interface ProductGridProps {
  products: Product[];
  onProductSelect?: (product: Product) => void;
  selectedIds?: number[];
}

interface ProductSearchProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

interface ProductFilterProps {
  categories: string[];
  series: string[];
  selectedCategory: string | null;
  selectedSeries: string | null;
  onCategoryChange: (category: string | null) => void;
  onSeriesChange: (series: string | null) => void;
}

// Skills
interface SkillCardProps {
  skill: Skill;
  onClick: () => void;
}

interface SkillDetailProps {
  skill: Skill;
  onDependencyClick: (skillName: string) => void;
}

interface CategoryFilterProps {
  categories: string[];
  selected: string | null;
  onChange: (category: string | null) => void;
}

// Prompts
interface PromptCardProps {
  prompt: ExamplePrompt;
  onClick: () => void;
  onUse: () => void;
}

interface PromptDetailProps {
  prompt: ExamplePrompt;
  onUseInWizard: () => void;
}

// Wizard
interface StepIndicatorProps {
  steps: { id: number; label: string }[];
  currentStep: number;
  completedSteps: number[];
}

interface ProductSelectorProps {
  selectedProducts: Product[];
  onAdd: (product: Product) => void;
  onRemove: (productId: number) => void;
}

interface LayoutOptionsProps {
  options: LayoutOptions;
  onChange: (options: Partial<LayoutOptions>) => void;
}

interface PromptInputProps {
  value: string;
  onChange: (value: string) => void;
  maxLength: number;
  examplePrompts: string[];
  onExampleSelect: (prompt: string) => void;
}

interface ImageUploadProps {
  image: { base64: string; mimeType: string } | null;
  onUpload: (image: { base64: string; mimeType: string }) => void;
  onRemove: () => void;
  maxSizeMb: number;
  acceptedTypes: string[];
}

interface ChatInterfaceProps {
  messages: ChatMessage[];
  onSendMessage: (content: string) => void;
  isLoading: boolean;
}

// Preview
interface LayoutPreviewProps {
  html: string;
  css: string;
  zoom: number;
  pageFormat: string;
}

interface VariantSelectorProps {
  variants: VariantResponse[];
  selectedId: string | null;
  onChange: (variantId: string) => void;
}

interface CodeEditorProps {
  html: string;
  css: string;
  onHtmlChange: (html: string) => void;
  onCssChange: (css: string) => void;
  onSave: () => void;
}

interface PdfExportProps {
  layoutId: string;
  variantId: string;
  presets: PrintPreset[];
  onExport: (preset: string) => void;
  isExporting: boolean;
}
```

### Wizard Store

```typescript
// src/store/wizardStore.ts
interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface WizardState {
  // Navigation
  currentStep: 1 | 2 | 3 | 4;
  setStep: (step: 1 | 2 | 3 | 4) => void;
  nextStep: () => void;
  prevStep: () => void;

  // Step 1: Products
  selectedProducts: Product[];
  addProduct: (product: Product) => void;
  removeProduct: (productId: number) => void;
  clearProducts: () => void;

  // Step 2: Options
  options: LayoutOptions;
  setOptions: (options: Partial<LayoutOptions>) => void;

  // Step 3: Prompt
  prompt: string;
  setPrompt: (prompt: string) => void;
  referenceImage: { base64: string; mimeType: string } | null;
  setReferenceImage: (image: { base64: string; mimeType: string } | null) => void;

  // Chat
  chatHistory: ChatMessage[];
  addChatMessage: (message: Omit<ChatMessage, 'id' | 'timestamp'>) => void;
  clearChat: () => void;

  // Step 4: Result
  generatedLayout: LayoutResponse | null;
  setGeneratedLayout: (layout: LayoutResponse | null) => void;
  selectedVariantId: string | null;
  setSelectedVariantId: (id: string | null) => void;

  // Status
  isGenerating: boolean;
  setIsGenerating: (value: boolean) => void;
  error: string | null;
  setError: (error: string | null) => void;

  // Reset
  reset: () => void;
}
```

## Data Models

### Product

```typescript
interface Product {
  id: number;
  name: string;
  shortDescription: string;
  description: string;
  longDescription: string;
  category: string;
  series: string;
  specs: Record<string, string>;
  highlights: string[];
  imageUrl: string;
  priceEur: number | null;
}
```

### Layout Types

```typescript
interface LayoutOptions {
  pageFormat?: 'A4' | 'A5' | 'A6' | 'DL' | 'SQUARE';
  style?: 'modern' | 'technical' | 'premium' | 'eco' | 'dynamic';
  variantCount?: number;
  includeSpecs?: boolean;
  complexStrategy?: boolean;
}

interface TextToLayoutRequest {
  productIds: number[];
  options?: LayoutOptions;
  prompt: string;
}

interface ImageToLayoutRequest {
  productIds: number[];
  options?: LayoutOptions;
  prompt?: string;
  imageBase64: string;
  imageMimeType: string;
}

interface LayoutResponse {
  id: string;
  status: string;
  generatedAt: string;
  pageFormat: string;
  variants: VariantResponse[];
  variantCount: number;
}

interface VariantResponse {
  id: string;
  html: string;
  css: string;
}
```

### Skill Types

```typescript
interface Skill {
  name: string;
  category: string;
  content: string;
  dependencies: string[];
  priority: number;
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

### PDF Types

```typescript
interface PdfGenerateRequest {
  layoutId: string;
  variantId?: string;
  preset?: string;
}

interface PdfGenerateResponse {
  pdfId: string;
  downloadUrl: string;
}

interface PrintPreset {
  name: string;
  description: string;
  dpi: number;
  bleedMm: number;
  cropMarks: boolean;
}
```

### Image Types

```typescript
interface ImageUploadResponse {
  imageId: string;
  url: string;
  mimeType: string;
  expiresAt: string;
}
```

### Error Types

```typescript
interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
```

### Page Format Dimensions

```typescript
const PAGE_FORMAT_DIMENSIONS: Record<string, { width: number; height: number; label: string }> = {
  A4: { width: 210, height: 297, label: '210×297mm' },
  A5: { width: 148, height: 210, label: '148×210mm' },
  A6: { width: 105, height: 148, label: '105×148mm' },
  DL: { width: 99, height: 210, label: '99×210mm' },
  SQUARE: { width: 210, height: 210, label: '210×210mm' },
};
```

### Style Badge Colors

```typescript
const STYLE_BADGE_COLORS: Record<string, BadgeVariant> = {
  modern: 'blue',
  technical: 'default',
  premium: 'purple',
  eco: 'green',
  dynamic: 'orange',
};
```



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: UI Component Variant Mapping

*For any* UI component (Button, Badge, Toast) and any valid variant input, the component SHALL render with the correct styling class corresponding to that variant.

**Validates: Requirements 2.3, 2.5, 2.9**

### Property 2: Responsive Sidebar Behavior

*For any* viewport width, the Sidebar component SHALL:
- Be hidden (hamburger menu) when width < 768px
- Be fixed at 240px when 768px ≤ width < 1280px
- Be fixed at 280px when width ≥ 1280px

**Validates: Requirements 3.5, 3.6, 3.7**

### Property 3: API Client Error Handling

*For any* HTTP response with status code ≥ 400, the API client SHALL throw an ApiError containing the correct status, error type, and message from the response body.

**Validates: Requirements 4.3**

### Property 4: Dashboard Data Aggregation

*For any* list of products, the Dashboard aggregation functions SHALL:
- Count total products correctly
- Group products by category with correct counts
- Group products by series with correct counts
- Calculate price distribution buckets correctly

**Validates: Requirements 6.2, 6.3, 6.4, 6.5**

### Property 5: Product Filtering and Sorting

*For any* product list and filter/sort criteria (search query, category, series, sort column, sort direction), the filtering function SHALL return only products matching ALL active filters, sorted according to the specified criteria.

**Validates: Requirements 6.6, 6.11, 6.12**

### Property 6: Skill Display and Filtering

*For any* skill and category filter selection:
- The SkillCard SHALL render all required fields (name, category, priority)
- When a category is selected, only skills of that category SHALL be displayed
- The SkillDetailModal SHALL render all fields including markdown content

**Validates: Requirements 7.4, 7.5, 7.7**

### Property 7: Prompt Display and Filtering

*For any* example prompt and filter combination (style, format):
- The PromptCard SHALL render all required fields (title, description, style, format)
- When filters are applied, only prompts matching ALL filters SHALL be displayed
- The PromptDetailModal SHALL render all fields including full prompt text

**Validates: Requirements 8.4, 8.5, 8.7**

### Property 8: Wizard Store State Management

*For any* sequence of wizard store operations:
- currentStep SHALL always be in range [1, 4]
- nextStep from step 4 SHALL remain at step 4
- prevStep from step 1 SHALL remain at step 1
- addProduct SHALL add product if not already present
- removeProduct SHALL remove only the specified product
- prompt length SHALL not exceed 5000 characters
- reset SHALL restore all state to initial values

**Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.6, 9.10, 9.11, 9.12**

### Property 9: Error Message Mapping

*For any* HTTP error status code, the error handler SHALL return the correct German error message:
- 400 → "Ungültige Eingabe. Bitte überprüfen Sie Ihre Daten."
- 404 → "Die angeforderte Ressource wurde nicht gefunden."
- 413 → "Die Datei ist zu groß. Maximale Größe: 10MB."
- 502 → "Der KI-Service ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut."
- 500 → "Ein unerwarteter Fehler ist aufgetreten."

**Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5**

### Property 10: Style to Badge Color Mapping

*For any* layout style value (modern, technical, premium, eco, dynamic), the style-to-badge-color mapping function SHALL return the correct badge variant:
- modern → blue
- technical → default
- premium → purple
- eco → green
- dynamic → orange

**Validates: Requirements 2.5**

### Property 11: Page Format Dimensions

*For any* page format value (A4, A5, A6, DL, SQUARE), the format-to-dimensions mapping SHALL return the correct width, height, and label string.

**Validates: Requirements 11.2**

### Property 12: Character Counter Validation

*For any* prompt input string, the character counter SHALL:
- Display the correct current character count
- Indicate when the limit (5000) is approached or exceeded
- Prevent submission when prompt is empty or exceeds limit

**Validates: Requirements 12.2, 12.10, 12.11**

## Error Handling

### API Error Handling Strategy

```typescript
// Error handling flow
try {
  const response = await apiClient<T>(endpoint, options);
  return response;
} catch (error) {
  if (error instanceof ApiError) {
    // Handle known API errors
    showToast(getErrorMessage(error.status), 'error');
  } else {
    // Handle network or unknown errors
    showToast('Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung.', 'error');
  }
  throw error;
}
```

### Error Message Mapping

```typescript
const ERROR_MESSAGES: Record<number, string> = {
  400: 'Ungültige Eingabe. Bitte überprüfen Sie Ihre Daten.',
  404: 'Die angeforderte Ressource wurde nicht gefunden.',
  413: 'Die Datei ist zu groß. Maximale Größe: 10MB.',
  502: 'Der KI-Service ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut.',
  500: 'Ein unerwarteter Fehler ist aufgetreten.',
};

function getErrorMessage(status: number): string {
  return ERROR_MESSAGES[status] || ERROR_MESSAGES[500];
}
```

### Component-Level Error Handling

- **Query Errors**: React Query's `isError` and `error` states for data fetching
- **Mutation Errors**: `onError` callbacks for mutations with toast notifications
- **Form Validation**: React Hook Form validation with inline error messages
- **File Upload Errors**: Size and type validation before upload attempt

### Error Boundaries

```typescript
// Top-level error boundary for unexpected React errors
class ErrorBoundary extends React.Component {
  state = { hasError: false };
  
  static getDerivedStateFromError() {
    return { hasError: true };
  }
  
  render() {
    if (this.state.hasError) {
      return <ErrorFallback onRetry={() => window.location.reload()} />;
    }
    return this.props.children;
  }
}
```

## Testing Strategy

### Testing Framework

- **Vitest**: Test runner compatible with Vite
- **React Testing Library**: Component testing
- **fast-check**: Property-based testing library for TypeScript
- **MSW (Mock Service Worker)**: API mocking for integration tests

### Test Categories

#### Unit Tests

Unit tests verify specific examples and edge cases:

- Component rendering with various props
- Utility function edge cases
- Store action handlers
- API client configuration

#### Property-Based Tests

Property-based tests verify universal properties across all inputs using fast-check:

```typescript
import fc from 'fast-check';
import { describe, it, expect } from 'vitest';

// Example: Property 8 - Wizard Store State Management
describe('Wizard Store State Management', () => {
  it('currentStep should always be in range [1, 4]', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: -100, max: 100 }),
        (step) => {
          const store = useWizardStore.getState();
          store.setStep(step as any);
          const currentStep = useWizardStore.getState().currentStep;
          return currentStep >= 1 && currentStep <= 4;
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

### Property Test Configuration

- Minimum 100 iterations per property test
- Each property test references its design document property
- Tag format: `Feature: catalogforge-frontend, Property {number}: {property_text}`

### Test File Structure

```
src/
├── api/
│   └── __tests__/
│       ├── client.test.ts          # Unit tests
│       └── client.property.test.ts # Property tests for Property 3
├── components/
│   └── ui/
│       └── __tests__/
│           ├── Button.test.tsx
│           └── Badge.property.test.tsx # Property tests for Property 1, 10
├── store/
│   └── __tests__/
│       └── wizardStore.property.test.ts # Property tests for Property 8
├── utils/
│   └── __tests__/
│       ├── formatters.test.ts
│       ├── filters.property.test.ts # Property tests for Property 5, 6, 7
│       └── errors.property.test.ts  # Property tests for Property 9
└── pages/
    └── Dashboard/
        └── __tests__/
            └── aggregations.property.test.ts # Property tests for Property 4
```

### Integration Tests

Integration tests verify component interactions with mocked APIs:

- Page-level rendering with data
- User interaction flows
- Navigation between pages
- Form submission flows

### Test Commands

```bash
# Run all tests
npm run test

# Run unit tests only
npm run test:unit

# Run property tests only
npm run test:property

# Run with coverage
npm run test:coverage

# Run in watch mode
npm run test:watch
```

### Coverage Requirements

- Minimum 80% line coverage for utility functions
- All property tests must pass with 100 iterations
- Critical paths (wizard flow, API calls) must have integration tests
