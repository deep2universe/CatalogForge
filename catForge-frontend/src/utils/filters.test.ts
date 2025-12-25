import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { filterProducts, sortProducts, filterSkills, sortSkills } from './filters';
import type { Product, Skill } from '@/api';

/**
 * Property 5: Product Filtering and Sorting
 * Validates: Requirements 6.6, 6.11, 6.12
 */
describe('Product Filtering and Sorting', () => {
  const productArbitrary = fc.record({
    id: fc.integer({ min: 1, max: 1000 }),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    shortDescription: fc.string({ minLength: 1, maxLength: 100 }),
    description: fc.string(),
    longDescription: fc.string(),
    category: fc.constantFrom('Fernverkehr', 'Verteiler', 'Bau', 'Elektro'),
    series: fc.constantFrom('Actros', 'Arocs', 'eActros', 'Atego'),
    specs: fc.dictionary(fc.string(), fc.string()),
    highlights: fc.array(fc.string()),
    imageUrl: fc.string(),
    priceEur: fc.option(fc.integer({ min: 10000, max: 500000 }), { nil: null }),
  }) as fc.Arbitrary<Product>;

  // Property: Filtering by category returns only products of that category
  it('should filter products by category correctly', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom('Fernverkehr', 'Verteiler', 'Bau', 'Elektro'),
        (products, category) => {
          const filtered = filterProducts(products, { category });
          filtered.forEach((product) => {
            expect(product.category).toBe(category);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by series returns only products of that series
  it('should filter products by series correctly', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom('Actros', 'Arocs', 'eActros', 'Atego'),
        (products, series) => {
          const filtered = filterProducts(products, { series });
          filtered.forEach((product) => {
            expect(product.series).toBe(series);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering never increases the number of products
  it('should never increase product count when filtering', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 20 }),
        fc.record({
          category: fc.option(fc.constantFrom('Fernverkehr', 'Verteiler', 'Bau', 'Elektro'), { nil: undefined }),
          series: fc.option(fc.constantFrom('Actros', 'Arocs', 'eActros', 'Atego'), { nil: undefined }),
          search: fc.option(fc.string({ maxLength: 20 }), { nil: undefined }),
        }),
        (products, filters) => {
          const filtered = filterProducts(products, filters);
          expect(filtered.length).toBeLessThanOrEqual(products.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sorting preserves all elements
  it('should preserve all elements when sorting', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 0, maxLength: 20 }),
        fc.constantFrom('name', 'price', 'category', 'series') as fc.Arbitrary<'name' | 'price' | 'category' | 'series'>,
        fc.constantFrom('asc', 'desc') as fc.Arbitrary<'asc' | 'desc'>,
        (products, sortBy, direction) => {
          const sorted = sortProducts(products, sortBy, direction);
          expect(sorted.length).toBe(products.length);
          // All original IDs should be present
          const originalIds = products.map((p) => p.id).sort();
          const sortedIds = sorted.map((p) => p.id).sort();
          expect(sortedIds).toEqual(originalIds);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sorting by name produces alphabetically ordered results
  it('should sort by name in correct order', () => {
    fc.assert(
      fc.property(
        fc.array(productArbitrary, { minLength: 2, maxLength: 20 }),
        fc.constantFrom('asc', 'desc') as fc.Arbitrary<'asc' | 'desc'>,
        (products, direction) => {
          const sorted = sortProducts(products, 'name', direction);
          for (let i = 1; i < sorted.length; i++) {
            const comparison = sorted[i - 1].name.localeCompare(sorted[i].name, 'de');
            if (direction === 'asc') {
              expect(comparison).toBeLessThanOrEqual(0);
            } else {
              expect(comparison).toBeGreaterThanOrEqual(0);
            }
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});

describe('Skill Filtering and Sorting', () => {
  const skillArbitrary = fc.record({
    name: fc.string({ minLength: 1, maxLength: 30 }),
    category: fc.constantFrom('core', 'styles', 'formats'),
    content: fc.string(),
    dependencies: fc.array(fc.string()),
    priority: fc.integer({ min: 0, max: 100 }),
  }) as fc.Arbitrary<Skill>;

  // Property: Filtering skills by category returns only skills of that category
  it('should filter skills by category correctly', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom('core', 'styles', 'formats'),
        (skills, category) => {
          const filtered = filterSkills(skills, category);
          filtered.forEach((skill) => {
            expect(skill.category).toBe(category);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sorting skills by priority produces ordered results
  it('should sort skills by priority in ascending order', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 2, maxLength: 20 }),
        (skills) => {
          const sorted = sortSkills(skills);
          for (let i = 1; i < sorted.length; i++) {
            expect(sorted[i - 1].priority).toBeLessThanOrEqual(sorted[i].priority);
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
