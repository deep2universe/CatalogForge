import { create } from 'zustand';
import type { Product, LayoutOptions, LayoutResponse, PageFormat, LayoutStyle } from '@/api';

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

const initialOptions: LayoutOptions = {
  pageFormat: 'A4' as PageFormat,
  style: 'modern' as LayoutStyle,
  variantCount: 2,
  includeSpecs: true,
  complexStrategy: false,
};

const createWelcomeMessage = (): ChatMessage => ({
  id: 'welcome',
  role: 'assistant',
  content: 'Willkommen! Beschreiben Sie, wie Ihr Katalog aussehen soll. Ich generiere dann passende Layout-Varianten für Sie.',
  timestamp: new Date(),
});

export const useWizardStore = create<WizardState>((set) => ({
  // Navigation
  currentStep: 1,
  setStep: (step) => set({ currentStep: step }),
  nextStep: () =>
    set((state) => ({
      currentStep: Math.min(state.currentStep + 1, 4) as 1 | 2 | 3 | 4,
    })),
  prevStep: () =>
    set((state) => ({
      currentStep: Math.max(state.currentStep - 1, 1) as 1 | 2 | 3 | 4,
    })),

  // Products
  selectedProducts: [],
  addProduct: (product) =>
    set((state) => ({
      selectedProducts: state.selectedProducts.some((p) => p.id === product.id)
        ? state.selectedProducts
        : [...state.selectedProducts, product],
    })),
  removeProduct: (productId) =>
    set((state) => ({
      selectedProducts: state.selectedProducts.filter((p) => p.id !== productId),
    })),
  clearProducts: () => set({ selectedProducts: [] }),

  // Options
  options: initialOptions,
  setOptions: (options) =>
    set((state) => ({
      options: { ...state.options, ...options },
    })),

  // Prompt
  prompt: '',
  setPrompt: (prompt) => set({ prompt }),
  referenceImage: null,
  setReferenceImage: (image) => set({ referenceImage: image }),

  // Chat
  chatHistory: [createWelcomeMessage()],
  addChatMessage: (message) =>
    set((state) => ({
      chatHistory: [
        ...state.chatHistory,
        {
          ...message,
          id: crypto.randomUUID(),
          timestamp: new Date(),
        },
      ],
    })),
  clearChat: () => set({ chatHistory: [createWelcomeMessage()] }),

  // Result
  generatedLayout: null,
  setGeneratedLayout: (layout) => set({ generatedLayout: layout }),
  selectedVariantId: null,
  setSelectedVariantId: (id) => set({ selectedVariantId: id }),

  // Status
  isGenerating: false,
  setIsGenerating: (value) => set({ isGenerating: value }),
  error: null,
  setError: (error) => set({ error }),

  // Reset
  reset: () =>
    set({
      currentStep: 1,
      selectedProducts: [],
      options: initialOptions,
      prompt: '',
      referenceImage: null,
      chatHistory: [createWelcomeMessage()],
      generatedLayout: null,
      selectedVariantId: null,
      isGenerating: false,
      error: null,
    }),
}));
