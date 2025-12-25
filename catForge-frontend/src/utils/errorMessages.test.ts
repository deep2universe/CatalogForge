import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { getErrorMessage, errorMessages, isKnownErrorCode } from './errorMessages';

/**
 * Property 9: Error Message Mapping
 * Validates: Requirements 15.1, 15.2, 15.3, 15.4, 15.5
 */
describe('Error Message Mapping', () => {
  const knownCodes = [400, 404, 413, 500, 502, 503];

  // Property: Known error codes always return their specific message
  it('should return specific message for known error codes', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(...knownCodes),
        (code) => {
          const message = getErrorMessage(code);
          expect(message).toBe(errorMessages[code]);
          expect(message.length).toBeGreaterThan(0);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: Unknown error codes always return fallback message
  it('should return fallback message for unknown error codes', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }).filter((n) => !knownCodes.includes(n)),
        (code) => {
          const message = getErrorMessage(code);
          expect(message).toBe(errorMessages[500]);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: isKnownErrorCode correctly identifies known codes
  it('should correctly identify known error codes', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (code) => {
          const isKnown = isKnownErrorCode(code);
          expect(isKnown).toBe(knownCodes.includes(code));
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: All error messages are non-empty German strings
  it('should have non-empty German messages for all known codes', () => {
    knownCodes.forEach((code) => {
      const message = errorMessages[code];
      expect(message).toBeDefined();
      expect(message.length).toBeGreaterThan(10);
      // German messages typically contain umlauts or specific words
      expect(message).toMatch(/[äöüßÄÖÜ]|Bitte|Fehler|nicht|ist/);
    });
  });

  // Property: getErrorMessage is deterministic
  it('should produce deterministic results', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (code) => {
          const result1 = getErrorMessage(code);
          const result2 = getErrorMessage(code);
          expect(result1).toBe(result2);
        }
      ),
      { numRuns: 100 }
    );
  });
});
