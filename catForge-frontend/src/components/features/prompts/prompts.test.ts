import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import type { ExamplePrompt, LayoutStyle, PageFormat } from '@/api';
import { filterPrompts } from '@/utils';

/**
 * Property 7: Prompt Display and Filtering
 * Validates: Requirements 8.4, 8.5, 8.7
 */
describe('Prompt Display and Filtering', () => {
  const styles: LayoutStyle[] = ['modern', 'technical', 'premium', 'eco', 'dynamic'];
  const formats: PageFormat[] = ['A4', 'A5', 'A6', 'DL', 'SQUARE'];

  const promptArbitrary = fc.record({
    id: fc.uuid(),
    title: fc.string({ minLength: 1, maxLength: 50 }),
    description: fc.string({ minLength: 0, maxLength: 200 }),
    prompt: fc.string({ minLength: 10, maxLength: 500 }),
    skills: fc.array(fc.string({ minLength: 1, maxLength: 20 }), { maxLength: 5 }),
    style: fc.constantFrom(...styles),
    format: fc.constantFrom(...formats),
  }) as fc.Arbitrary<ExamplePrompt>;

  // Property: Filtering by 'all' style returns all prompts
  it('should return all prompts when filtering by "all" style', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 0, maxLength: 20 }),
        (prompts) => {
          const filtered = filterPrompts(prompts, 'all', undefined);
          expect(filtered.length).toBe(prompts.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by 'all' format returns all prompts
  it('should return all prompts when filtering by "all" format', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 0, maxLength: 20 }),
        (prompts) => {
          const filtered = filterPrompts(prompts, undefined, 'all');
          expect(filtered.length).toBe(prompts.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by style returns only prompts of that style
  it('should filter prompts by style correctly', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom(...styles),
        (prompts, style) => {
          const filtered = filterPrompts(prompts, style, undefined);
          filtered.forEach((prompt) => {
            expect(prompt.style).toBe(style);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by format returns only prompts of that format
  it('should filter prompts by format correctly', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom(...formats),
        (prompts, format) => {
          const filtered = filterPrompts(prompts, undefined, format);
          filtered.forEach((prompt) => {
            expect(prompt.format).toBe(format);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering by both style and format returns intersection
  it('should filter prompts by both style and format correctly', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 1, maxLength: 20 }),
        fc.constantFrom(...styles),
        fc.constantFrom(...formats),
        (prompts, style, format) => {
          const filtered = filterPrompts(prompts, style, format);
          filtered.forEach((prompt) => {
            expect(prompt.style).toBe(style);
            expect(prompt.format).toBe(format);
          });
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering never increases the number of prompts
  it('should never increase prompt count when filtering', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 0, maxLength: 20 }),
        fc.option(fc.constantFrom(...styles, 'all'), { nil: undefined }),
        fc.option(fc.constantFrom(...formats, 'all'), { nil: undefined }),
        (prompts, style, format) => {
          const filtered = filterPrompts(prompts, style as LayoutStyle | 'all', format as PageFormat | 'all');
          expect(filtered.length).toBeLessThanOrEqual(prompts.length);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: All prompts have required fields
  it('should have all required fields in prompts', () => {
    fc.assert(
      fc.property(
        promptArbitrary,
        (prompt) => {
          expect(prompt.id).toBeDefined();
          expect(prompt.title.length).toBeGreaterThan(0);
          expect(prompt.prompt.length).toBeGreaterThan(0);
          expect(styles).toContain(prompt.style);
          expect(formats).toContain(prompt.format);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Filtering is idempotent
  it('should be idempotent when filtering', () => {
    fc.assert(
      fc.property(
        fc.array(promptArbitrary, { minLength: 0, maxLength: 20 }),
        fc.constantFrom(...styles),
        fc.constantFrom(...formats),
        (prompts, style, format) => {
          const filtered1 = filterPrompts(prompts, style, format);
          const filtered2 = filterPrompts(filtered1, style, format);
          expect(filtered1).toEqual(filtered2);
        }
      ),
      { numRuns: 100 }
    );
  });
});
