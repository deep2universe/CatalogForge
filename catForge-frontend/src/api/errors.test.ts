import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { ApiError, getErrorMessage, errorMessages } from './errors';
import type { ErrorResponse } from './types';

/**
 * Property 3: API Client Error Handling
 * Validates: Requirements 4.3
 */
describe('API Error Handling', () => {
  // Property: For any valid ErrorResponse, ApiError correctly extracts status
  it('should correctly extract status from any ErrorResponse', () => {
    fc.assert(
      fc.property(
        fc.record({
          timestamp: fc.string(),
          status: fc.integer({ min: 100, max: 599 }),
          error: fc.string(),
          message: fc.string(),
          path: fc.string(),
        }),
        (errorResponse: ErrorResponse) => {
          const apiError = new ApiError(errorResponse);
          expect(apiError.status).toBe(errorResponse.status);
          expect(apiError.message).toBe(errorResponse.message);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: isNotFound is true iff status === 404
  it('should correctly identify 404 errors', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (status) => {
          const errorResponse: ErrorResponse = {
            timestamp: new Date().toISOString(),
            status,
            error: 'Test',
            message: 'Test message',
            path: '/test',
          };
          const apiError = new ApiError(errorResponse);
          expect(apiError.isNotFound).toBe(status === 404);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: isValidationError is true iff status === 400
  it('should correctly identify validation errors', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (status) => {
          const errorResponse: ErrorResponse = {
            timestamp: new Date().toISOString(),
            status,
            error: 'Test',
            message: 'Test message',
            path: '/test',
          };
          const apiError = new ApiError(errorResponse);
          expect(apiError.isValidationError).toBe(status === 400);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: isServerError is true iff status >= 500
  it('should correctly identify server errors', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (status) => {
          const errorResponse: ErrorResponse = {
            timestamp: new Date().toISOString(),
            status,
            error: 'Test',
            message: 'Test message',
            path: '/test',
          };
          const apiError = new ApiError(errorResponse);
          expect(apiError.isServerError).toBe(status >= 500);
        }
      ),
      { numRuns: 100 }
    );
  });

  // Property: getErrorMessage returns a known message for known codes, fallback otherwise
  it('should return appropriate error messages for any status code', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 100, max: 599 }),
        (status) => {
          const message = getErrorMessage(status);
          if (status in errorMessages) {
            expect(message).toBe(errorMessages[status]);
          } else {
            expect(message).toBe(errorMessages[500]);
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
