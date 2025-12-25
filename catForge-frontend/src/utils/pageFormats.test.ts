import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { PageFormat } from '@/api';
import {
  pageFormatDimensions,
  pageFormats,
  getPageDimensions,
  getAspectRatio,
  isPortrait,
  isLandscape,
  isSquare,
} from './pageFormats';

/**
 * Property 11: Page Format Dimensions
 * Validates: Requirements 11.2
 */
describe('Page Format Dimensions', () => {
  // Property: All page formats have positive dimensions
  it('should have positive dimensions for all formats', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...pageFormats),
        (format: PageFormat) => {
          const dims = getPageDimensions(format);
          expect(dims.width).toBeGreaterThan(0);
          expect(dims.height).toBeGreaterThan(0);
          expect(dims.unit).toBe('mm');
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Aspect ratio is always positive
  it('should have positive aspect ratio for all formats', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...pageFormats),
        (format: PageFormat) => {
          const ratio = getAspectRatio(format);
          expect(ratio).toBeGreaterThan(0);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Portrait, landscape, and square are mutually exclusive
  it('should have mutually exclusive orientation classifications', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...pageFormats),
        (format: PageFormat) => {
          const portrait = isPortrait(format);
          const landscape = isLandscape(format);
          const square = isSquare(format);
          
          // Exactly one should be true
          const trueCount = [portrait, landscape, square].filter(Boolean).length;
          expect(trueCount).toBe(1);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Square format has equal width and height
  it('should have equal dimensions for square format', () => {
    const dims = getPageDimensions('SQUARE');
    expect(dims.width).toBe(dims.height);
    expect(isSquare('SQUARE')).toBe(true);
  });

  // Property: A-series formats follow ISO 216 ratios (approximately 1:√2)
  it('should have correct A-series aspect ratios', () => {
    const sqrt2 = Math.SQRT2;
    const tolerance = 0.01;

    ['A4', 'A5', 'A6'].forEach((format) => {
      const dims = getPageDimensions(format as PageFormat);
      const ratio = dims.height / dims.width;
      expect(Math.abs(ratio - sqrt2)).toBeLessThan(tolerance);
    });
  });

  // Property: All formats have a label defined
  it('should have labels for all formats', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...pageFormats),
        (format: PageFormat) => {
          const dims = getPageDimensions(format);
          expect(dims.label).toBeDefined();
          expect(dims.label.length).toBeGreaterThan(0);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: getPageDimensions is deterministic
  it('should return consistent dimensions', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...pageFormats),
        (format: PageFormat) => {
          const dims1 = getPageDimensions(format);
          const dims2 = getPageDimensions(format);
          expect(dims1).toEqual(dims2);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Verify specific known dimensions
  it('should have correct specific dimensions', () => {
    expect(pageFormatDimensions.A4).toEqual({
      width: 210,
      height: 297,
      unit: 'mm',
      label: 'A4 (210×297mm)',
    });
    expect(pageFormatDimensions.DL).toEqual({
      width: 99,
      height: 210,
      unit: 'mm',
      label: 'DL (99×210mm)',
    });
  });
});
