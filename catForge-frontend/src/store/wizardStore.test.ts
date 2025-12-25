import { describe, it, expect, beforeEach } from 'vitest';
import * as fc from 'fast-check';
import { useWizardStore } from './wizardStore';
import type { Product, PageFormat, LayoutStyle } from '@/api';

/**
 * Property 8: Wizard Store State Management
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.6, 9.10, 9.11, 9.12
 */
describe('Wizard Store State Management', () => {
  beforeEach(() => {
    useWizardStore.getState().reset();
  });

  const productArbitrary = fc.record({
    id: fc.integer({ min: 1, max: 1000 }),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    shortDescription: fc.string(),
    description: fc.string(),
    longDescription: fc.string(),
    category: fc.constantFrom('Fernverkehr', 'Verteiler', 'Bau', 'Elektro'),
    series: fc.constantFrom('Actros', 'Arocs', 'eActros', 'Atego'),
    specs: fc.dictionary(fc.string(), fc.string()),
    highlights: fc.array(fc.string()),
    imageUrl: fc.string(),
    priceEur: fc.option(fc.integer({ min: 10000, max: 500000 }), { nil: null }),
  }) as fc.Arbitrary<Product>;

  // Property: Adding a product increases selectedProducts length by 1 (if not duplicate)
  it('should add unique products to selection', () => {
    fc.assert(
      fc.property(
        productArbitrary,
        (product) => {
          useWizardStore.getState().reset();
          const initialLength = useWizardStore.getState().selectedProducts.length;
          useWizardStore.getState().addProduct(product);
          const newLength = useWizardStore.getState().selectedProducts.length;
          expect(newLength).toBe(initialLength + 1);
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Adding duplicate product does not increase length
  it('should not add duplicate products', () => {
    fc.assert(
      fc.property(
        productArbitrary,
        (product) => {
          const store = useWizardStore.getState();
          store.reset();
          store.addProduct(product);
          const lengthAfterFirst = useWizardStore.getState().selectedProducts.length;
          store.addProduct(product);
          expect(useWizardStore.getState().selectedProducts.length).toBe(lengthAfterFirst);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Removing a product decreases length by 1 (if exists)
  it('should remove products from selection', () => {
    fc.assert(
      fc.property(
        productArbitrary,
        (product) => {
          const store = useWizardStore.getState();
          store.reset();
          store.addProduct(product);
          const lengthBefore = useWizardStore.getState().selectedProducts.length;
          store.removeProduct(product.id);
          expect(useWizardStore.getState().selectedProducts.length).toBe(lengthBefore - 1);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Step navigation stays within bounds [1, 4]
  it('should keep step within valid bounds', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 10 }),
        (times) => {
          const store = useWizardStore.getState();
          store.reset();
          for (let i = 0; i < times; i++) {
            store.nextStep();
          }
          const step = useWizardStore.getState().currentStep;
          expect(step).toBeGreaterThanOrEqual(1);
          expect(step).toBeLessThanOrEqual(4);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: prevStep never goes below 1
  it('should not go below step 1', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 10 }),
        (times) => {
          const store = useWizardStore.getState();
          store.reset();
          for (let i = 0; i < times; i++) {
            store.prevStep();
          }
          expect(useWizardStore.getState().currentStep).toBe(1);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: setOptions merges with existing options
  it('should merge options correctly', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('A4', 'A5', 'A6', 'DL', 'SQUARE') as fc.Arbitrary<PageFormat>,
        fc.constantFrom('modern', 'technical', 'premium', 'eco', 'dynamic') as fc.Arbitrary<LayoutStyle>,
        fc.integer({ min: 1, max: 5 }),
        (format, style, variantCount) => {
          const store = useWizardStore.getState();
          store.reset();
          store.setOptions({ pageFormat: format });
          store.setOptions({ style });
          store.setOptions({ variantCount });
          
          const options = useWizardStore.getState().options;
          expect(options.pageFormat).toBe(format);
          expect(options.style).toBe(style);
          expect(options.variantCount).toBe(variantCount);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: setPrompt updates prompt correctly
  it('should update prompt correctly', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 0, maxLength: 5000 }),
        (prompt) => {
          const store = useWizardStore.getState();
          store.reset();
          store.setPrompt(prompt);
          expect(useWizardStore.getState().prompt).toBe(prompt);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: reset returns to initial state
  it('should reset to initial state', () => {
    fc.assert(
      fc.property(
        productArbitrary,
        fc.string({ minLength: 1, maxLength: 100 }),
        (product, prompt) => {
          const store = useWizardStore.getState();
          store.addProduct(product);
          store.setPrompt(prompt);
          store.nextStep();
          store.reset();
          
          const state = useWizardStore.getState();
          expect(state.currentStep).toBe(1);
          expect(state.selectedProducts.length).toBe(0);
          expect(state.prompt).toBe('');
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Chat messages are added with unique IDs
  it('should add chat messages with unique IDs', () => {
    fc.assert(
      fc.property(
        fc.array(fc.string({ minLength: 1, maxLength: 100 }), { minLength: 2, maxLength: 10 }),
        (messages) => {
          const store = useWizardStore.getState();
          store.reset();
          
          messages.forEach((content) => {
            store.addChatMessage({ role: 'user', content });
          });
          
          const chatHistory = useWizardStore.getState().chatHistory;
          const ids = chatHistory.map((m) => m.id);
          const uniqueIds = new Set(ids);
          expect(uniqueIds.size).toBe(ids.length);
        }
      ),
      { numRuns: 100 }
    );
  });
});
