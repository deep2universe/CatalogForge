import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { LayoutStyle } from '@/api/types';
import { styleToBadgeVariant, type BadgeVariant } from './Badge';

/**
 * Property 10: Style to Badge Color Mapping
 * Validates: Requirements 2.5
 */
describe('Badge Style Mapping', () => {
  const validStyles: LayoutStyle[] = ['modern', 'technical', 'premium', 'eco', 'dynamic'];
  const validBadgeVariants: BadgeVariant[] = ['default', 'blue', 'green', 'purple', 'yellow', 'orange', 'red'];

  // Property: Every LayoutStyle maps to a valid BadgeVariant
  it('should map every layout style to a valid badge variant', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...validStyles),
        (style) => {
          const badgeVariant = styleToBadgeVariant[style];
          expect(validBadgeVariants).toContain(badgeVariant);
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Style mapping is deterministic (same input always produces same output)
  it('should produce deterministic mappings', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...validStyles),
        (style) => {
          const result1 = styleToBadgeVariant[style];
          const result2 = styleToBadgeVariant[style];
          expect(result1).toBe(result2);
          return true;
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: All styles have a mapping defined
  it('should have mappings for all layout styles', () => {
    validStyles.forEach((style) => {
      expect(styleToBadgeVariant[style]).toBeDefined();
    });
  });

  // Property: Expected specific mappings
  it('should map styles to expected badge variants', () => {
    expect(styleToBadgeVariant['modern']).toBe('blue');
    expect(styleToBadgeVariant['technical']).toBe('default');
    expect(styleToBadgeVariant['premium']).toBe('purple');
    expect(styleToBadgeVariant['eco']).toBe('green');
    expect(styleToBadgeVariant['dynamic']).toBe('orange');
  });
});
