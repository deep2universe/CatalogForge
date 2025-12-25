import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { Product } from '@/api';
import {
  countByCategory,
  countBySeries,
  calculatePriceRange,
  getTotalCount,
  getUniqueCategories,
  getUniqueSeries,
} from './aggregations';

/**
 * Property 4: Dashboard Data Aggregation
 * Validates: Requirements 6.2, 6.3, 6.4, 6.5
 */
describe('Dashboard Data Aggregation', () => {
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

  // Property: Sum of category counts equals total product count
  it('should have category counts sum equal to total products', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 50 }),
        (products) => {
          const categoryCounts = countByCategory(products);
          const sum = categoryCounts.reduce((acc, c) => acc + c.value, 0);
          expect(sum).toBe(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sum of series counts equals total product count
  it('should have series counts sum equal to total products', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 50 }),
        (products) => {
          const seriesCounts = countBySeries(products);
          const sum = seriesCounts.reduce((acc, c) => acc + c.value, 0);
          expect(sum).toBe(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: getTotalCount returns array length
  it('should return correct total count', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 50 }),
        (products) => {
          expect(getTotalCount(products)).toBe(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Unique categories count <= total products
  it('should have unique categories count <= total products', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 50 }),
        (products) => {
          const uniqueCategories = getUniqueCategories(products);
          expect(uniqueCategories.length).toBeLessThanOrEqual(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Unique series count <= total products
  it('should have unique series count <= total products', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 50 }),
        (products) => {
          const uniqueSeries = getUniqueSeries(products);
          expect(uniqueSeries.length).toBeLessThanOrEqual(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Price range min <= avg <= max
  it('should have valid price range ordering', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 1, maxLength: 50 }),
        (products) => {
          const range = calculatePriceRange(products);
          if (range) {
            expect(range.min).toBeLessThanOrEqual(range.avg);
            expect(range.avg).toBeLessThanOrEqual(range.max);
          }
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Empty products returns null price range
  it('should return null price range for products without prices', () => {
    const productsWithoutPrices: Product[] = [
      {
        id: 1,
        name: 'Test',
        shortDescription: '',
        description: '',
        longDescription: '',
        category: 'Fernverkehr',
        series: 'Actros',
        specs: {},
        highlights: [],
        imageUrl: '',
        priceEur: null,
      },
    ];
    expect(calculatePriceRange(productsWithoutPrices)).toBeNull();
  });

  // Property: Category counts are sorted descending by value
  it('should sort category counts in descending order', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 2, maxLength: 50 }),
        (products) => {
          const counts = countByCategory(products);
          for (let i = 1; i < counts.length; i++) {
            expect(counts[i - 1].value).toBeGreaterThanOrEqual(counts[i].value);
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
