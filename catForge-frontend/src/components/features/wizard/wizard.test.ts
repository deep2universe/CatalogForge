import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { 
  getPromptCharacterCount, 
  validatePromptLength, 
  MAX_PROMPT_LENGTH,
  validateImage,
  validateFileSize,
  validateImageType,
  MAX_FILE_SIZE,
  ALLOWED_IMAGE_TYPES
} from '@/utils';

describe('Character Counter Validation', () => {
  it('should correctly count characters for any string', () => {
    fc.assert(
      fc.property(fc.string(), (text) => {
        const result = getPromptCharacterCount(text);
        expect(result.current).toBe(text.length);
        expect(result.max).toBe(MAX_PROMPT_LENGTH);
        expect(result.remaining).toBe(MAX_PROMPT_LENGTH - text.length);
      })
    );
  });

  it('should correctly identify over-limit prompts', () => {
    fc.assert(
      fc.property(fc.string(), (text) => {
        const result = getPromptCharacterCount(text);
        expect(result.isOverLimit).toBe(text.length > MAX_PROMPT_LENGTH);
      })
    );
  });

  it('should validate prompt length correctly', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: MAX_PROMPT_LENGTH }),
        (text) => {
          const result = validatePromptLength(text);
          expect(result.valid).toBe(true);
        }
      )
    );
  });

  it('should reject empty prompts', () => {
    const result = validatePromptLength('');
    expect(result.valid).toBe(false);
    expect(result.message).toBeDefined();
  });

  it('should reject prompts exceeding max length', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: MAX_PROMPT_LENGTH + 1, maxLength: MAX_PROMPT_LENGTH + 100 }),
        (text) => {
          const result = validatePromptLength(text);
          expect(result.valid).toBe(false);
          expect(result.message).toBeDefined();
        }
      )
    );
  });

  it('should have consistent remaining calculation', () => {
    fc.assert(
      fc.property(fc.string(), (text) => {
        const result = getPromptCharacterCount(text);
        expect(result.current + result.remaining).toBe(result.max);
      })
    );
  });
});

describe('Image Validation', () => {
  const createMockFile = (size: number, type: string): File => {
    const blob = new Blob(['x'.repeat(size)], { type });
    return new File([blob], 'test.jpg', { type });
  };

  it('should accept valid image types', () => {
    ALLOWED_IMAGE_TYPES.forEach((type) => {
      const file = createMockFile(1000, type);
      const result = validateImageType(file);
      expect(result.valid).toBe(true);
    });
  });

  it('should reject invalid image types', () => {
    const invalidTypes = ['image/gif', 'image/bmp', 'application/pdf', 'text/plain'];
    invalidTypes.forEach((type) => {
      const file = createMockFile(1000, type);
      const result = validateImageType(file);
      expect(result.valid).toBe(false);
    });
  });

  it('should accept files under size limit', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: MAX_FILE_SIZE }),
        (size) => {
          const file = createMockFile(size, 'image/jpeg');
          const result = validateFileSize(file);
          expect(result.valid).toBe(true);
        }
      )
    );
  });

  it('should reject files over size limit', () => {
    const file = createMockFile(MAX_FILE_SIZE + 1, 'image/jpeg');
    const result = validateFileSize(file);
    expect(result.valid).toBe(false);
  });

  it('should validate complete image correctly', () => {
    const validFile = createMockFile(1000, 'image/jpeg');
    expect(validateImage(validFile).valid).toBe(true);

    const invalidType = createMockFile(1000, 'image/gif');
    expect(validateImage(invalidType).valid).toBe(false);

    const tooLarge = createMockFile(MAX_FILE_SIZE + 1, 'image/jpeg');
    expect(validateImage(tooLarge).valid).toBe(false);
  });
});
