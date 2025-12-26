# CatalogForge: React → Vue 3.5 Migration Guide

> **Ziel:** Vollständige Migration des CatalogForge React-Frontends zu Vue 3.5 mit identischer Funktionalität  
> **Zielgruppe:** Senior Developer  
> **Erstellungsdatum:** Dezember 2024

---

## Inhaltsverzeichnis

1. [Executive Summary](#1-executive-summary)
2. [Technologie-Stack Mapping](#2-technologie-stack-mapping)
3. [Vue 3.5 Key Features & Breaking Changes](#3-vue-35-key-features--breaking-changes)
4. [Architektur-Konzepte im Vergleich](#4-architektur-konzepte-im-vergleich)
5. [Projekt-Setup](#5-projekt-setup)
6. [Komponenten-Migration](#6-komponenten-migration)
7. [State Management: Zustand → Pinia](#7-state-management-zustand--pinia)
8. [Data Fetching: TanStack Query](#8-data-fetching-tanstack-query)
9. [Routing: React Router → Vue Router](#9-routing-react-router--vue-router)
10. [Hooks → Composables](#10-hooks--composables)
11. [Testing-Migration](#11-testing-migration)
12. [Styling & Tailwind](#12-styling--tailwind)
13. [Migrations-Checkliste](#13-migrations-checkliste)
14. [Recherche-Quellen](#14-recherche-quellen)

---

## 1. Executive Summary

### Warum diese Migration?

Die Migration von React zu Vue 3.5 bietet folgende Vorteile:

- **Einfachere Reaktivität**: Vue's Proxy-basiertes Reaktivitätssystem erfordert keine expliziten Dependencies-Arrays
- **56% weniger Memory-Usage** in Vue 3.5 durch optimiertes Reaktivitätssystem
- **Bis zu 10x schnellere Array-Operationen** bei großen Datensätzen
- **Composition API**: Konzeptionell ähnlich zu React Hooks, aber ohne die "Rules of Hooks"-Einschränkungen
- **Bessere TypeScript-Integration** durch native Unterstützung

### Migrations-Strategie

```
Phase 1: Setup & Infrastruktur (1-2 Tage)
    ↓
Phase 2: API-Layer & Types (1 Tag)
    ↓
Phase 3: UI-Komponenten Bottom-Up (3-5 Tage)
    ↓
Phase 4: State Management (1-2 Tage)
    ↓
Phase 5: Pages & Routing (2-3 Tage)
    ↓
Phase 6: Testing & QA (2-3 Tage)
```

---

## 2. Technologie-Stack Mapping

| React (Current)        | Vue 3.5 (Target)           | Änderungsgrad |
|------------------------|----------------------------|---------------|
| React 18               | Vue 3.5.26                 | Vollständig   |
| TypeScript 5.3         | TypeScript 5.3             | Identisch     |
| Vite 5                 | Vite 5                     | Minimal       |
| React Router 6         | Vue Router 4               | API-Änderung  |
| TanStack Query (React) | TanStack Query (Vue)       | API-Ähnlich   |
| Zustand                | Pinia                      | Konzept-Shift |
| Tailwind CSS 3.4       | Tailwind CSS 3.4           | Identisch     |
| Recharts               | vue-chartjs / Recharts     | Adapter       |
| React Hook Form        | VeeValidate / FormKit      | Alternative   |
| Vitest + RTL           | Vitest + Vue Test Utils    | API-Änderung  |
| Lucide React           | Lucide Vue Next            | Minimal       |

### Package.json Transformation

```json
// Vue 3.5 Dependencies
{
  "dependencies": {
    "vue": "^3.5.26",
    "vue-router": "^4.4.0",
    "pinia": "^2.2.0",
    "@tanstack/vue-query": "^5.60.0",
    "vue-chartjs": "^5.3.0",
    "chart.js": "^4.4.0",
    "@vueuse/core": "^11.0.0",
    "lucide-vue-next": "^0.330.0",
    "clsx": "^2.1.0",
    "tailwind-merge": "^2.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.1.0",
    "vue-tsc": "^2.1.0",
    "@vue/test-utils": "^2.4.0",
    "vitest": "^1.3.0",
    "typescript": "^5.3.3"
  }
}
```

---

## 3. Vue 3.5 Key Features & Breaking Changes

### 3.5 Release Notes (September 2024)

> *Quelle: [Vue Blog - Announcing Vue 3.5](https://blog.vuejs.org/posts/vue-3-5)*

#### Neue Features

**1. Reactive Props Destructure (Stable)**
```vue
<script setup lang="ts">
// Vue 3.5: Reaktivität bleibt erhalten!
const { count, title } = defineProps<{
  count: number
  title: string
}>()

// Funktioniert automatisch reaktiv - kein toRef() mehr nötig
watchEffect(() => {
  console.log(count) // Reagiert auf Änderungen
})
</script>
```

**2. useTemplateRef() API**
```vue
<script setup lang="ts">
import { useTemplateRef } from 'vue'

// Neuer, typsicherer Weg für Template-Refs
const inputRef = useTemplateRef<HTMLInputElement>('input')

onMounted(() => {
  inputRef.value?.focus()
})
</script>

<template>
  <input ref="input" />
</template>
```

**3. useId() für SSR-sichere IDs**
```vue
<script setup>
import { useId } from 'vue'

const id = useId() // Stabile ID zwischen Server und Client
</script>

<template>
  <label :for="id">Name</label>
  <input :id="id" />
</template>
```

**4. Deferred Teleport**
```vue
<template>
  <!-- defer: Target muss nicht sofort existieren -->
  <Teleport to="#modal-container" defer>
    <Modal />
  </Teleport>
</template>
```

**5. Watch mit pause/resume**
```ts
const { pause, resume, stop } = watch(source, callback)

pause()  // Pausiert den Watcher
resume() // Setzt fort
stop()   // Stoppt permanent
```

**6. onWatcherCleanup()**
```ts
import { watch, onWatcherCleanup } from 'vue'

watch(id, async (newId) => {
  const controller = new AbortController()
  
  // Cleanup bei Re-Run oder Stop
  onWatcherCleanup(() => controller.abort())
  
  const data = await fetch(`/api/${newId}`, {
    signal: controller.signal
  })
})
```

#### Performance-Verbesserungen

- **56% weniger Memory-Usage** durch Reaktivitäts-Refactoring
- **Bis zu 10x schnellere Array-Operationen** (shift, unshift, splice)
- Optimiertes Tracking für große, tiefe reaktive Arrays

#### Keine Breaking Changes!

Vue 3.5 ist ein Minor Release ohne Breaking Changes zu Vue 3.4.

---

## 4. Architektur-Konzepte im Vergleich

### Komponenten-Struktur

```
React (JSX)                          Vue (SFC)
─────────────────────────────────    ─────────────────────────────────
function Component({ prop }) {       <script setup lang="ts">
  const [state, setState] =          import { ref } from 'vue'
    useState(initial)
                                     const props = defineProps<{
  useEffect(() => {                    prop: string
    // Side effect                   }>()
  }, [dep])
                                     const state = ref(initial)
  return (
    <div>{state}</div>               // Lifecycle in setup automatisch
  )                                  </script>
}
                                     <template>
                                       <div>{{ state }}</div>
                                     </template>
```

### Reaktivitäts-Paradigmen

| Aspekt | React | Vue 3.5 |
|--------|-------|---------|
| State-Mutation | Immutable (setState) | Mutable (ref.value = x) |
| Re-Render Trigger | Explizit (setState) | Automatisch (Proxy) |
| Dependencies | Manuelles Array | Automatisches Tracking |
| Setup Execution | Bei jedem Render | Einmal bei Mount |
| Memoization | useMemo, useCallback | computed (automatisch cached) |

> *"The difference between Vue Composition API and React Hooks is that React Hooks can run multiple times during rendering; Vue's setup function runs only once while creating a component."*  
> — [LogRocket Blog](https://blog.logrocket.com/vue-composition-api-vs-react-hooks/)

### Konzept-Mapping

```
React                    Vue 3.5
────────────────────────────────────────────
useState                 ref / reactive
useEffect                watch / watchEffect / onMounted
useMemo                  computed
useCallback              (nicht nötig - setup läuft einmal)
useRef                   ref / useTemplateRef
useContext               provide / inject
React.memo               defineAsyncComponent mit suspense
Suspense                 <Suspense>
ErrorBoundary            onErrorCaptured + errorComponent
Fragment (<></>)         <template> (automatisch)
children                 <slot />
render props             scoped slots
HOC                      composables
```

---

## 5. Projekt-Setup

### 5.1 Vite-Konfiguration

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### 5.2 TypeScript-Konfiguration

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": [
    "src/**/*.ts",
    "src/**/*.tsx",
    "src/**/*.vue"
  ],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 5.3 Projekt-Struktur (Vue-Konvention)

```
src/
├── api/                    # API-Client (identisch zu React)
│   ├── client.ts
│   ├── errors.ts
│   ├── types.ts
│   └── index.ts
├── assets/
│   └── styles/
│       └── globals.css
├── components/
│   ├── ui/                 # Basis-Komponenten
│   │   ├── Button.vue
│   │   ├── Card.vue
│   │   ├── Modal.vue
│   │   └── index.ts
│   ├── charts/             # Chart-Komponenten
│   │   ├── BarChart.vue
│   │   ├── PieChart.vue
│   │   └── StatCard.vue
│   ├── layout/             # Layout-Komponenten
│   │   ├── AppLayout.vue
│   │   ├── Header.vue
│   │   └── Sidebar.vue
│   └── features/           # Feature-Komponenten
│       ├── wizard/
│       ├── products/
│       └── preview/
├── composables/            # Vue Composables (≈ React Hooks)
│   ├── useProducts.ts
│   ├── useLayouts.ts
│   ├── useDebounce.ts
│   └── index.ts
├── stores/                 # Pinia Stores
│   ├── wizardStore.ts
│   └── index.ts
├── router/                 # Vue Router
│   └── index.ts
├── views/                  # Page-Komponenten
│   ├── DashboardView.vue
│   ├── WizardView.vue
│   └── PreviewView.vue
├── utils/                  # Utility-Funktionen (identisch)
│   ├── cn.ts
│   ├── formatters.ts
│   └── validators.ts
├── App.vue
└── main.ts
```

### 5.4 Main Entry Point

```typescript
// src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import router from './router'
import App from './App.vue'
import './assets/styles/globals.css'

const app = createApp(App)

// Plugins
app.use(createPinia())
app.use(router)
app.use(VueQueryPlugin, {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        staleTime: 5 * 60 * 1000,
        retry: 1,
      },
    },
  },
})

app.mount('#app')
```

---

## 6. Komponenten-Migration

### 6.1 Basis-Pattern: React → Vue SFC

**React (Button.tsx)**
```tsx
import { forwardRef, type ButtonHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', children, ...props }, ref) => {
    return (
      <button
        ref={ref}
        className={cn(
          'inline-flex items-center justify-center rounded-lg font-medium',
          variantClasses[variant],
          sizeClasses[size],
          className
        )}
        {...props}
      >
        {children}
      </button>
    );
  }
);
```

**Vue (Button.vue)**
```vue
<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { cn } from '@/utils/cn'

interface Props {
  variant?: 'primary' | 'secondary' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
})

const attrs = useAttrs()

const variantClasses = {
  primary: 'bg-pastel-blue text-neutral-800 hover:bg-pastel-blue-dark',
  secondary: 'bg-white border border-neutral-200 text-neutral-800',
  ghost: 'bg-transparent text-neutral-600 hover:bg-neutral-100',
}

const sizeClasses = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-sm',
  lg: 'px-6 py-3 text-base',
}

const buttonClass = computed(() =>
  cn(
    'inline-flex items-center justify-center rounded-lg font-medium',
    'transition-colors focus:outline-none focus:ring-2',
    variantClasses[props.variant],
    sizeClasses[props.size],
    props.class
  )
)
</script>

<template>
  <button :class="buttonClass" v-bind="attrs">
    <slot />
  </button>
</template>
```

### 6.2 Card Component Migration

**React (Card.tsx)**
```tsx
interface CardProps {
  children: ReactNode;
  hoverable?: boolean;
  onClick?: () => void;
  className?: string;
}

export function Card({ children, hoverable, onClick, className }: CardProps) {
  return (
    <div
      onClick={onClick}
      className={cn(
        'bg-white rounded-xl border border-neutral-100 shadow-card',
        hoverable && 'hover:shadow-card-hover cursor-pointer transition-shadow',
        className
      )}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn('px-6 py-4 border-b border-neutral-100', className)}>{children}</div>;
}

export function CardContent({ children, className }: { children: ReactNode; className?: string }) {
  return <div className={cn('px-6 py-4', className)}>{children}</div>;
}
```

**Vue (Card.vue)**
```vue
<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/utils/cn'

interface Props {
  hoverable?: boolean
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  hoverable: false,
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const cardClass = computed(() =>
  cn(
    'bg-white rounded-xl border border-neutral-100 shadow-card',
    props.hoverable && 'hover:shadow-card-hover cursor-pointer transition-shadow',
    props.class
  )
)
</script>

<template>
  <div :class="cardClass" @click="emit('click', $event)">
    <slot />
  </div>
</template>
```

**CardHeader.vue & CardContent.vue**
```vue
<!-- CardHeader.vue -->
<script setup lang="ts">
import { cn } from '@/utils/cn'

defineProps<{ class?: string }>()
</script>

<template>
  <div :class="cn('px-6 py-4 border-b border-neutral-100', $props.class)">
    <slot />
  </div>
</template>

<!-- CardContent.vue -->
<script setup lang="ts">
import { cn } from '@/utils/cn'

defineProps<{ class?: string }>()
</script>

<template>
  <div :class="cn('px-6 py-4', $props.class)">
    <slot />
  </div>
</template>
```

### 6.3 Conditional Rendering

**React**
```tsx
{isLoading && <Spinner />}
{error && <ErrorMessage error={error} />}
{data && data.map(item => <Item key={item.id} {...item} />)}
{items.length === 0 ? <Empty /> : <List items={items} />}
```

**Vue**
```vue
<template>
  <Spinner v-if="isLoading" />
  <ErrorMessage v-else-if="error" :error="error" />
  <template v-else-if="data">
    <Item v-for="item in data" :key="item.id" v-bind="item" />
  </template>
  
  <Empty v-if="items.length === 0" />
  <List v-else :items="items" />
</template>
```

### 6.4 Event Handling

**React**
```tsx
<button onClick={() => handleClick(id)}>Click</button>
<input onChange={(e) => setValue(e.target.value)} />
<form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
```

**Vue**
```vue
<template>
  <button @click="handleClick(id)">Click</button>
  <input @input="(e) => setValue((e.target as HTMLInputElement).value)" />
  <!-- oder mit v-model -->
  <input v-model="value" />
  <form @submit.prevent="handleSubmit">
</template>
```

### 6.5 Slots vs Children

**React (mit children)**
```tsx
function Modal({ children, header, footer }) {
  return (
    <div className="modal">
      <div className="modal-header">{header}</div>
      <div className="modal-body">{children}</div>
      <div className="modal-footer">{footer}</div>
    </div>
  );
}

// Usage
<Modal 
  header={<h2>Title</h2>}
  footer={<Button>Close</Button>}
>
  Content here
</Modal>
```

**Vue (mit Slots)**
```vue
<!-- Modal.vue -->
<template>
  <div class="modal">
    <div class="modal-header">
      <slot name="header" />
    </div>
    <div class="modal-body">
      <slot /> <!-- Default Slot -->
    </div>
    <div class="modal-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<!-- Usage -->
<Modal>
  <template #header>
    <h2>Title</h2>
  </template>
  
  Content here
  
  <template #footer>
    <Button>Close</Button>
  </template>
</Modal>
```

---

## 7. State Management: Zustand → Pinia

### 7.1 Konzeptvergleich

| Zustand | Pinia | Unterschied |
|---------|-------|-------------|
| `create()` | `defineStore()` | Ähnliche Factory-Funktion |
| State direkt | `state: () => ({})` | Funktion für SSR-Kompatibilität |
| Actions in create | `actions: {}` | Separates Objekt |
| Keine Getters | `getters: {}` | Computed-ähnlich |
| `useStore()` | `useStore()` | Identisch |
| Middleware | Pinia Plugins | Flexibler |

### 7.2 WizardStore Migration

**Zustand (React)**
```typescript
// store/wizardStore.ts
import { create } from 'zustand';
import type { Product, LayoutOptions, LayoutResponse } from '@/api';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface WizardState {
  currentStep: 1 | 2 | 3 | 4;
  selectedProducts: Product[];
  options: LayoutOptions;
  prompt: string;
  chatHistory: ChatMessage[];
  generatedLayouts: LayoutResponse | null;
  
  setStep: (step: 1 | 2 | 3 | 4) => void;
  nextStep: () => void;
  prevStep: () => void;
  addProduct: (product: Product) => void;
  removeProduct: (productId: number) => void;
  setOptions: (options: Partial<LayoutOptions>) => void;
  setPrompt: (prompt: string) => void;
  addChatMessage: (message: Omit<ChatMessage, 'id' | 'timestamp'>) => void;
  setGeneratedLayouts: (layouts: LayoutResponse) => void;
  reset: () => void;
}

const initialState = {
  currentStep: 1 as const,
  selectedProducts: [],
  options: {
    pageFormat: 'A4' as const,
    style: 'modern' as const,
    variantCount: 3,
    includeSpecs: true,
  },
  prompt: '',
  chatHistory: [],
  generatedLayouts: null,
};

export const useWizardStore = create<WizardState>((set, get) => ({
  ...initialState,
  
  setStep: (step) => set({ currentStep: step }),
  
  nextStep: () => set((state) => ({
    currentStep: Math.min(state.currentStep + 1, 4) as 1 | 2 | 3 | 4,
  })),
  
  prevStep: () => set((state) => ({
    currentStep: Math.max(state.currentStep - 1, 1) as 1 | 2 | 3 | 4,
  })),
  
  addProduct: (product) => set((state) => {
    if (state.selectedProducts.some((p) => p.id === product.id)) {
      return state;
    }
    return { selectedProducts: [...state.selectedProducts, product] };
  }),
  
  removeProduct: (productId) => set((state) => ({
    selectedProducts: state.selectedProducts.filter((p) => p.id !== productId),
  })),
  
  setOptions: (options) => set((state) => ({
    options: { ...state.options, ...options },
  })),
  
  setPrompt: (prompt) => set({ prompt }),
  
  addChatMessage: (message) => set((state) => ({
    chatHistory: [
      ...state.chatHistory,
      { ...message, id: crypto.randomUUID(), timestamp: new Date() },
    ],
  })),
  
  setGeneratedLayouts: (layouts) => set({ generatedLayouts: layouts }),
  
  reset: () => set(initialState),
}));
```

**Pinia (Vue)**
```typescript
// stores/wizardStore.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Product, LayoutOptions, LayoutResponse, PageFormat, LayoutStyle } from '@/api'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

// Composition API Style (empfohlen für Vue 3.5)
export const useWizardStore = defineStore('wizard', () => {
  // State
  const currentStep = ref<1 | 2 | 3 | 4>(1)
  const selectedProducts = ref<Product[]>([])
  const options = ref<LayoutOptions>({
    pageFormat: 'A4',
    style: 'modern',
    variantCount: 3,
    includeSpecs: true,
  })
  const prompt = ref('')
  const chatHistory = ref<ChatMessage[]>([])
  const generatedLayouts = ref<LayoutResponse | null>(null)

  // Getters
  const productCount = computed(() => selectedProducts.value.length)
  const hasProducts = computed(() => selectedProducts.value.length > 0)
  const canProceed = computed(() => {
    switch (currentStep.value) {
      case 1: return hasProducts.value
      case 2: return true
      case 3: return prompt.value.length > 0
      case 4: return generatedLayouts.value !== null
      default: return false
    }
  })

  // Actions
  function setStep(step: 1 | 2 | 3 | 4) {
    currentStep.value = step
  }

  function nextStep() {
    if (currentStep.value < 4) {
      currentStep.value = (currentStep.value + 1) as 1 | 2 | 3 | 4
    }
  }

  function prevStep() {
    if (currentStep.value > 1) {
      currentStep.value = (currentStep.value - 1) as 1 | 2 | 3 | 4
    }
  }

  function addProduct(product: Product) {
    if (!selectedProducts.value.some((p) => p.id === product.id)) {
      selectedProducts.value.push(product)
    }
  }

  function removeProduct(productId: number) {
    const index = selectedProducts.value.findIndex((p) => p.id === productId)
    if (index > -1) {
      selectedProducts.value.splice(index, 1)
    }
  }

  function clearProducts() {
    selectedProducts.value = []
  }

  function setOptions(newOptions: Partial<LayoutOptions>) {
    options.value = { ...options.value, ...newOptions }
  }

  function setPrompt(newPrompt: string) {
    prompt.value = newPrompt
  }

  function addChatMessage(message: Omit<ChatMessage, 'id' | 'timestamp'>) {
    chatHistory.value.push({
      ...message,
      id: crypto.randomUUID(),
      timestamp: new Date(),
    })
  }

  function setGeneratedLayouts(layouts: LayoutResponse) {
    generatedLayouts.value = layouts
  }

  function reset() {
    currentStep.value = 1
    selectedProducts.value = []
    options.value = {
      pageFormat: 'A4',
      style: 'modern',
      variantCount: 3,
      includeSpecs: true,
    }
    prompt.value = ''
    chatHistory.value = []
    generatedLayouts.value = null
  }

  return {
    // State
    currentStep,
    selectedProducts,
    options,
    prompt,
    chatHistory,
    generatedLayouts,
    
    // Getters
    productCount,
    hasProducts,
    canProceed,
    
    // Actions
    setStep,
    nextStep,
    prevStep,
    addProduct,
    removeProduct,
    clearProducts,
    setOptions,
    setPrompt,
    addChatMessage,
    setGeneratedLayouts,
    reset,
  }
})
```

### 7.3 Store Usage in Komponenten

**React**
```tsx
function WizardPage() {
  const { currentStep, nextStep, selectedProducts } = useWizardStore();
  
  return (
    <div>
      <p>Step {currentStep}</p>
      <p>{selectedProducts.length} products selected</p>
      <button onClick={nextStep}>Next</button>
    </div>
  );
}
```

**Vue**
```vue
<script setup lang="ts">
import { useWizardStore } from '@/stores/wizardStore'
import { storeToRefs } from 'pinia'

const wizardStore = useWizardStore()

// Reaktive Referenzen extrahieren (wichtig für Reaktivität!)
const { currentStep, selectedProducts, productCount } = storeToRefs(wizardStore)

// Actions direkt vom Store
const { nextStep } = wizardStore
</script>

<template>
  <div>
    <p>Step {{ currentStep }}</p>
    <p>{{ productCount }} products selected</p>
    <button @click="nextStep">Next</button>
  </div>
</template>
```

---

## 8. Data Fetching: TanStack Query

### 8.1 Setup

```typescript
// main.ts
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 Minuten
      retry: 1,
    },
  },
})

app.use(VueQueryPlugin, { queryClient })
```

### 8.2 Query Hook Migration

**React (useProducts.ts)**
```typescript
import { useQuery } from '@tanstack/react-query';
import { getProducts } from '@/api';

export function useProducts(filters?: ProductFilters) {
  return useQuery({
    queryKey: ['products', filters],
    queryFn: () => getProducts(filters),
  });
}
```

**Vue (useProducts.ts)**
```typescript
// composables/useProducts.ts
import { useQuery } from '@tanstack/vue-query'
import { computed, type MaybeRef, toValue } from 'vue'
import { getProducts, type ProductFilters } from '@/api'

export function useProducts(filters?: MaybeRef<ProductFilters | undefined>) {
  return useQuery({
    queryKey: computed(() => ['products', toValue(filters)]),
    queryFn: () => getProducts(toValue(filters)),
  })
}
```

### 8.3 Mutation Hook Migration

**React**
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';

export function useGenerateLayout() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: generateLayout,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['layouts'] });
    },
  });
}
```

**Vue**
```typescript
// composables/useGenerateLayout.ts
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { generateLayout } from '@/api'

export function useGenerateLayout() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: generateLayout,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['layouts'] })
    },
  })
}
```

### 8.4 Verwendung in Komponenten

**React**
```tsx
function ProductList() {
  const { data, isLoading, error } = useProducts();
  
  if (isLoading) return <Spinner />;
  if (error) return <Error error={error} />;
  
  return (
    <ul>
      {data?.map(product => (
        <ProductCard key={product.id} product={product} />
      ))}
    </ul>
  );
}
```

**Vue**
```vue
<script setup lang="ts">
import { useProducts } from '@/composables/useProducts'
import Spinner from '@/components/ui/Spinner.vue'
import ProductCard from '@/components/features/products/ProductCard.vue'

const { data, isLoading, error } = useProducts()
</script>

<template>
  <Spinner v-if="isLoading" />
  <div v-else-if="error" class="error">{{ error.message }}</div>
  <ul v-else>
    <ProductCard 
      v-for="product in data" 
      :key="product.id" 
      :product="product" 
    />
  </ul>
</template>
```

---

## 9. Routing: React Router → Vue Router

### 9.1 Router-Konfiguration

**React Router**
```tsx
// App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/skills" element={<SkillExplorerPage />} />
          <Route path="/wizard" element={<CatalogWizardPage />} />
          <Route path="/preview/:id" element={<CatalogPreviewPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

**Vue Router**
```typescript
// router/index.ts
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
      },
      {
        path: 'skills',
        name: 'skills',
        component: () => import('@/views/SkillExplorerView.vue'),
      },
      {
        path: 'prompts',
        name: 'prompts',
        component: () => import('@/views/PromptExplorerView.vue'),
      },
      {
        path: 'wizard',
        name: 'wizard',
        component: () => import('@/views/CatalogWizardView.vue'),
      },
      {
        path: 'preview/:id',
        name: 'preview',
        component: () => import('@/views/CatalogPreviewView.vue'),
        props: true, // Route params als Props
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
```

### 9.2 Layout mit RouterView

**React (AppLayout.tsx)**
```tsx
import { Outlet } from 'react-router-dom';

export function AppLayout() {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <div className="flex-1">
        <Header />
        <main className="p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
```

**Vue (AppLayout.vue)**
```vue
<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<template>
  <div class="flex min-h-screen">
    <Sidebar />
    <div class="flex-1">
      <Header />
      <main class="p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
```

### 9.3 Navigation

**React**
```tsx
import { Link, useNavigate, useParams } from 'react-router-dom';

function Component() {
  const navigate = useNavigate();
  const { id } = useParams();
  
  return (
    <>
      <Link to="/wizard">Go to Wizard</Link>
      <button onClick={() => navigate(`/preview/${layoutId}`)}>
        Preview
      </button>
    </>
  );
}
```

**Vue**
```vue
<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// Route Params
const id = route.params.id

function goToPreview(layoutId: string) {
  router.push(`/preview/${layoutId}`)
  // oder mit named route:
  // router.push({ name: 'preview', params: { id: layoutId } })
}
</script>

<template>
  <RouterLink to="/wizard">Go to Wizard</RouterLink>
  <button @click="goToPreview(layoutId)">Preview</button>
</template>
```

---

## 10. Hooks → Composables

### 10.1 useDebounce

**React**
```typescript
// hooks/useDebounce.ts
import { useState, useEffect } from 'react';

export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}
```

**Vue**
```typescript
// composables/useDebounce.ts
import { ref, watch, type Ref } from 'vue'

export function useDebounce<T>(value: Ref<T>, delay: number): Ref<T> {
  const debouncedValue = ref(value.value) as Ref<T>

  let timeout: ReturnType<typeof setTimeout>

  watch(value, (newValue) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  })

  return debouncedValue
}

// Alternativ mit @vueuse/core (empfohlen):
// import { useDebouncedRef } from '@vueuse/core'
```

### 10.2 useImageUpload

**React**
```typescript
export function useImageUpload() {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadedImage, setUploadedImage] = useState<ImageUploadResponse | null>(null);
  const [error, setError] = useState<Error | null>(null);

  const uploadImage = async (file: File) => {
    setIsUploading(true);
    setError(null);
    try {
      const response = await uploadImageApi(file);
      setUploadedImage(response);
      return response;
    } catch (err) {
      setError(err as Error);
      throw err;
    } finally {
      setIsUploading(false);
    }
  };

  const clearImage = () => setUploadedImage(null);

  return { isUploading, uploadedImage, error, uploadImage, clearImage };
}
```

**Vue**
```typescript
// composables/useImageUpload.ts
import { ref } from 'vue'
import { uploadImage as uploadImageApi, type ImageUploadResponse } from '@/api'

export function useImageUpload() {
  const isUploading = ref(false)
  const uploadedImage = ref<ImageUploadResponse | null>(null)
  const error = ref<Error | null>(null)

  async function uploadImage(file: File) {
    isUploading.value = true
    error.value = null
    
    try {
      const response = await uploadImageApi(file)
      uploadedImage.value = response
      return response
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      isUploading.value = false
    }
  }

  function clearImage() {
    uploadedImage.value = null
  }

  return {
    isUploading,
    uploadedImage,
    error,
    uploadImage,
    clearImage,
  }
}
```

### 10.3 Lifecycle Hooks Mapping

```typescript
// React                    // Vue 3
// ─────────────────────────────────────────────
useEffect(() => {           onMounted(() => {
  // mount                    // mount
}, [])                      })

useEffect(() => {           watch(dep, () => {
  // on dep change            // on dep change
}, [dep])                   })

useEffect(() => {           watchEffect(() => {
  // runs on every dep        // auto-tracks deps
})                          })

useEffect(() => {           onMounted(() => {
  return () => {              onUnmounted(() => {
    // cleanup                  // cleanup
  }                           })
}, [])                      })

useLayoutEffect             onBeforeMount / flush: 'pre'
```

---

## 11. Testing-Migration

### 11.1 Vitest Setup für Vue

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{js,ts,vue}'],
    coverage: {
      reporter: ['text', 'json', 'html'],
      exclude: ['node_modules/', 'src/test/'],
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})
```

```typescript
// src/test/setup.ts
import { config } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'

// Global Stubs
config.global.stubs = {
  RouterLink: true,
  RouterView: true,
}
```

### 11.2 Komponenten-Tests

**React Testing Library**
```tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders children', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click</Button>);
    fireEvent.click(screen.getByText('Click'));
    expect(handleClick).toHaveBeenCalledOnce();
  });
});
```

**Vue Test Utils**
```typescript
// components/ui/Button.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from './Button.vue'

describe('Button', () => {
  it('renders slot content', () => {
    const wrapper = mount(Button, {
      slots: {
        default: 'Click me',
      },
    })
    expect(wrapper.text()).toContain('Click me')
  })

  it('emits click event when clicked', async () => {
    const wrapper = mount(Button)
    await wrapper.trigger('click')
    expect(wrapper.emitted('click')).toHaveLength(1)
  })

  it('applies variant classes', () => {
    const wrapper = mount(Button, {
      props: { variant: 'secondary' },
    })
    expect(wrapper.classes()).toContain('bg-white')
  })
})
```

### 11.3 Store-Tests mit Pinia

```typescript
// stores/wizardStore.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useWizardStore } from './wizardStore'

describe('WizardStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should start at step 1', () => {
    const store = useWizardStore()
    expect(store.currentStep).toBe(1)
  })

  it('should add products without duplicates', () => {
    const store = useWizardStore()
    const product = { id: 1, name: 'Test', category: 'Fernverkehr' }
    
    store.addProduct(product)
    store.addProduct(product) // Duplicate
    
    expect(store.selectedProducts).toHaveLength(1)
  })

  it('should navigate steps within bounds', () => {
    const store = useWizardStore()
    
    store.nextStep()
    store.nextStep()
    store.nextStep()
    store.nextStep() // Should stay at 4
    
    expect(store.currentStep).toBe(4)
    
    store.prevStep()
    expect(store.currentStep).toBe(3)
  })

  it('should reset to initial state', () => {
    const store = useWizardStore()
    
    store.addProduct({ id: 1, name: 'Test' })
    store.setPrompt('Test prompt')
    store.nextStep()
    
    store.reset()
    
    expect(store.currentStep).toBe(1)
    expect(store.selectedProducts).toHaveLength(0)
    expect(store.prompt).toBe('')
  })
})
```

### 11.4 Composable-Tests

```typescript
// composables/useDebounce.test.ts
import { describe, it, expect, vi } from 'vitest'
import { ref, nextTick } from 'vue'
import { useDebounce } from './useDebounce'

describe('useDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should debounce value changes', async () => {
    const value = ref('initial')
    const debounced = useDebounce(value, 300)

    expect(debounced.value).toBe('initial')

    value.value = 'changed'
    await nextTick()
    
    // Wert sollte noch nicht geändert sein
    expect(debounced.value).toBe('initial')

    // Timer voranschreiten
    vi.advanceTimersByTime(300)
    await nextTick()

    expect(debounced.value).toBe('changed')
  })
})
```

---

## 12. Styling & Tailwind

### 12.1 Tailwind-Konfiguration (identisch)

Die `tailwind.config.js` bleibt nahezu identisch:

```javascript
// tailwind.config.js
export default {
  content: ['./index.html', './src/**/*.{js,ts,vue}'], // .vue statt .jsx/.tsx
  theme: {
    extend: {
      colors: {
        pastel: {
          blue: { DEFAULT: '#A8D5E5', dark: '#7BC0D4', light: '#D4EAF2' },
          green: { DEFAULT: '#B5E5CF', dark: '#8AD4B5', light: '#DAF2E7' },
          // ... Rest identisch
        },
      },
    },
  },
  plugins: [],
}
```

### 12.2 cn() Utility (identisch)

```typescript
// utils/cn.ts
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

### 12.3 Scoped Styles in Vue

Vue bietet zusätzlich scoped CSS:

```vue
<template>
  <div class="container">
    <h1>Title</h1>
  </div>
</template>

<style scoped>
/* Nur für diese Komponente */
.container {
  @apply p-4 bg-white rounded-lg;
}

h1 {
  @apply text-2xl font-bold;
}
</style>
```

---

## 13. Migrations-Checkliste

### Phase 1: Setup ✅

- [ ] Vue 3.5 Projekt mit Vite erstellen
- [ ] TypeScript konfigurieren
- [ ] Tailwind CSS einrichten
- [ ] Path Aliases konfigurieren
- [ ] ESLint + Prettier für Vue

### Phase 2: API Layer ✅

- [ ] `api/client.ts` kopieren (keine Änderung nötig)
- [ ] `api/types.ts` kopieren (keine Änderung nötig)
- [ ] `api/errors.ts` kopieren (keine Änderung nötig)
- [ ] API-Funktionen migrieren

### Phase 3: UI-Komponenten

- [ ] Button.vue
- [ ] Card.vue, CardHeader.vue, CardContent.vue
- [ ] Input.vue
- [ ] Select.vue
- [ ] Modal.vue
- [ ] Spinner.vue
- [ ] Badge.vue
- [ ] Tabs.vue (mit provide/inject)
- [ ] Toast.vue (mit provide/inject)
- [ ] Toggle.vue
- [ ] Slider.vue

### Phase 4: Layout-Komponenten

- [ ] AppLayout.vue
- [ ] Header.vue
- [ ] Sidebar.vue
- [ ] PageContainer.vue

### Phase 5: Chart-Komponenten

- [ ] StatCard.vue
- [ ] BarChart.vue (vue-chartjs)
- [ ] PieChart.vue (vue-chartjs)

### Phase 6: Feature-Komponenten

**Products:**
- [ ] ProductCard.vue
- [ ] ProductGrid.vue
- [ ] ProductFilter.vue
- [ ] ProductSearch.vue

**Wizard:**
- [ ] StepIndicator.vue
- [ ] ProductSelector.vue
- [ ] LayoutOptions.vue
- [ ] PromptInput.vue
- [ ] ImageUpload.vue
- [ ] ChatInterface.vue

**Preview:**
- [ ] LayoutPreview.vue
- [ ] CodeEditor.vue
- [ ] VariantSelector.vue
- [ ] PdfExport.vue

**Skills & Prompts:**
- [ ] SkillCard.vue
- [ ] SkillDetail.vue
- [ ] CategoryFilter.vue
- [ ] PromptCard.vue
- [ ] PromptDetail.vue

### Phase 7: Composables

- [ ] useProducts.ts
- [ ] useLayouts.ts
- [ ] useSkills.ts
- [ ] usePdf.ts
- [ ] useDebounce.ts
- [ ] useImageUpload.ts

### Phase 8: Stores

- [ ] wizardStore.ts (Pinia)

### Phase 9: Views/Pages

- [ ] DashboardView.vue
- [ ] SkillExplorerView.vue
- [ ] PromptExplorerView.vue
- [ ] CatalogWizardView.vue
- [ ] CatalogPreviewView.vue

### Phase 10: Router

- [ ] router/index.ts
- [ ] Lazy Loading konfigurieren
- [ ] Navigation Guards (falls nötig)

### Phase 11: Global Setup

- [ ] main.ts (Pinia, Router, Vue Query)
- [ ] App.vue
- [ ] ErrorBoundary (onErrorCaptured)

### Phase 12: Testing

- [ ] Vitest Setup
- [ ] Komponenten-Tests migrieren
- [ ] Store-Tests migrieren
- [ ] Composable-Tests migrieren

---

## 14. Recherche-Quellen

### Offizielle Dokumentation

1. **Vue 3.5 Release Notes**
   - URL: https://blog.vuejs.org/posts/vue-3-5
   - Key: Reactive Props Destructure, useTemplateRef, 56% Memory Reduction

2. **Vue Composition API**
   - URL: https://vuejs.org/guide/extras/composition-api-faq
   - Key: Setup runs once vs React's multiple re-renders

3. **Vue TypeScript Guide**
   - URL: https://vuejs.org/guide/typescript/composition-api
   - Key: defineProps, defineEmits mit generics

4. **Pinia Documentation**
   - URL: https://pinia.vuejs.org/
   - Key: Composition API Style Stores

5. **Vue Router 4**
   - URL: https://router.vuejs.org/guide/
   - Key: createRouter, createWebHistory

6. **TanStack Query Vue**
   - URL: https://tanstack.com/query/v5/docs/framework/vue/overview
   - Key: useQuery mit reaktiven queryKeys

### Community Resources

7. **React to Vue Migration**
   - Source: DEV Community
   - URL: https://dev.to/nikhilverma/switching-from-react-to-vue-heres-what-to-expect-5i8
   - Key: Mutable state advantage, automatic reactivity

8. **Hooks vs Composables**
   - Source: LogRocket Blog
   - URL: https://blog.logrocket.com/vue-composition-api-vs-react-hooks/
   - Key: Vue setup runs once, React hooks run on every render

9. **React to Vue Patterns**
   - Source: Epicmax
   - URL: https://epicmax.co/react-vue
   - Key: Pattern mapping table

10. **Pinia vs Vuex**
    - Source: Various Medium articles
    - Key: 40% less boilerplate, no mutations needed

### Technische Vergleiche

11. **Vue 3.5 Features**
    - Source: Vue School
    - URL: https://vueschool.io/articles/vuejs-tutorials/whats-coming-in-vue-3-5/
    - Key: Lazy hydration, deferred teleport

12. **TypeScript Best Practices**
    - Source: Medium
    - URL: https://medium.com/@vasanthancomrads/strong-typing-in-vue-3-with-typescript-best-practices
    - Key: ref<T>, defineProps<T>, defineEmits<T>

---

## Anhang: Schnellreferenz

### Syntax-Cheatsheet

| React | Vue 3.5 |
|-------|---------|
| `useState(x)` | `ref(x)` |
| `setState(x)` | `state.value = x` |
| `useMemo(() => x, [d])` | `computed(() => x)` |
| `useEffect(() => {}, [])` | `onMounted(() => {})` |
| `useEffect(() => {}, [d])` | `watch(d, () => {})` |
| `useContext(Ctx)` | `inject(key)` |
| `<Ctx.Provider value={x}>` | `provide(key, x)` |
| `{condition && <X />}` | `<X v-if="condition" />` |
| `{items.map(i => <X key={i.id} />)}` | `<X v-for="i in items" :key="i.id" />` |
| `className={cn(...)}` | `:class="cn(...)"` |
| `onClick={handler}` | `@click="handler"` |
| `{children}` | `<slot />` |
| `<Link to="/x">` | `<RouterLink to="/x">` |
| `useNavigate()` | `useRouter()` |
| `useParams()` | `useRoute().params` |

---

*Dieser Guide wurde basierend auf Vue 3.5.26 und den aktuellen Best Practices erstellt.*
