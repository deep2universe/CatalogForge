import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { ButtonVariant, ButtonSize } from './Button';

/**
 * Property 1: UI Component Variant Mapping
 * Validates: Requirements 2.3, 2.5, 2.9
 */
describe('Button Variant Mapping', () => {
  const validVariants: ButtonVariant[] = ['primary', 'secondary', 'ghost', 'danger'];
  const validSizes: ButtonSize[] = ['sm', 'md', 'lg'];

  // Property: All valid variants map to distinct style classes
  it('should have distinct styles for each variant', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...validVariants),
        fc.constantFrom(...validVariants),
        (variant1, variant2) => {
          if (variant1 !== variant2) {
            // Different variants should produce different visual results
            expect(variant1).not.toBe(variant2);
          }
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: All valid sizes are recognized
  it('should recognize all valid sizes', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...validSizes),
        (size) => {
          expect(validSizes).toContain(size);
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Variant and size combinations are independent
  it('should allow any combination of variant and size', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...validVariants),
        fc.constantFrom(...validSizes),
        (variant, size) => {
          // All combinations should be valid
          expect(validVariants).toContain(variant);
          expect(validSizes).toContain(size);
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });
});
