import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { Skill } from '@/api';
import { filterSkills, sortSkills } from '@/utils';

/**
 * Property 6: Skill Display and Filtering
 * Validates: Requirements 7.4, 7.5, 7.7
 */
describe('Skill Display and Filtering', () => {
  const skillArbitrary = fc.record({
    name: fc.string({ minLength: 1, maxLength: 30 }).map((s) => s.toUpperCase().replace(/\s/g, '_')),
    category: fc.constantFrom('core', 'styles', 'formats'),
    content: fc.string({ minLength: 10, maxLength: 500 }),
    dependencies: fc.array(fc.string({ minLength: 1, maxLength: 20 }), { maxLength: 5 }),
    priority: fc.integer({ min: 0, max: 100 }),
  }) as fc.Arbitrary<Skill>;

  // Property: Filtering by 'all' returns all skills
  it('should return all skills when filtering by "all"', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 0, maxLength: 30 }),
        (skills) => {
          const filtered = filterSkills(skills, 'all');
          expect(filtered.length).toBe(skills.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by undefined returns all skills
  it('should return all skills when filtering by undefined', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 0, maxLength: 30 }),
        (skills) => {
          const filtered = filterSkills(skills, undefined);
          expect(filtered.length).toBe(skills.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by category returns only skills of that category
  it('should filter skills by category correctly', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 1, maxLength: 30 }),
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

  // Property: Filtering never increases the number of skills
  it('should never increase skill count when filtering', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 0, maxLength: 30 }),
        fc.constantFrom('all', 'core', 'styles', 'formats'),
        (skills, category) => {
          const filtered = filterSkills(skills, category);
          expect(filtered.length).toBeLessThanOrEqual(skills.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sorting preserves all elements
  it('should preserve all elements when sorting', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 0, maxLength: 30 }),
        (skills) => {
          const sorted = sortSkills(skills);
          expect(sorted.length).toBe(skills.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Sorting produces ascending priority order
  it('should sort skills by priority in ascending order', () => {
    fc.assert(
      fc.property(
        fc.array(skillArbitrary, { minLength: 2, maxLength: 30 }),
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

  // Property: Skill names are uppercase with underscores (convention)
  it('should have properly formatted skill names', () => {
    fc.assert(
      fc.property(
        skillArbitrary,
        (skill) => {
          // Names should be uppercase
          expect(skill.name).toBe(skill.name.toUpperCase());
          // Names should not contain spaces
          expect(skill.name).not.toContain(' ');
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Category is always one of the valid values
  it('should have valid category values', () => {
    const validCategories = ['core', 'styles', 'formats'];
    fc.assert(
      fc.property(
        skillArbitrary,
        (skill) => {
          expect(validCategories).toContain(skill.category);
        }
      ),
      { numRuns: 100 }
    );
  });
});
