export const MAX_PROMPT_LENGTH = 5000;
export const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

export function validatePromptLength(prompt: string): { valid: boolean; message?: string } {
  if (prompt.length === 0) {
    return { valid: false, message: 'Bitte geben Sie einen Prompt ein.' };
  }
  if (prompt.length > MAX_PROMPT_LENGTH) {
    return { valid: false, message: `Prompt darf maximal ${MAX_PROMPT_LENGTH} Zeichen haben.` };
  }
  return { valid: true };
}

export function validateFileSize(file: File): { valid: boolean; message?: string } {
  if (file.size > MAX_FILE_SIZE) {
    return { valid: false, message: 'Die Datei ist zu groß. Maximale Größe: 10MB.' };
  }
  return { valid: true };
}

export function validateImageType(file: File): { valid: boolean; message?: string } {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    return { valid: false, message: 'Nur JPG, PNG und WebP Dateien sind erlaubt.' };
  }
  return { valid: true };
}

export function validateImage(file: File): { valid: boolean; message?: string } {
  const typeValidation = validateImageType(file);
  if (!typeValidation.valid) return typeValidation;
  
  const sizeValidation = validateFileSize(file);
  if (!sizeValidation.valid) return sizeValidation;
  
  return { valid: true };
}

export function getPromptCharacterCount(prompt: string): {
  current: number;
  max: number;
  remaining: number;
  isOverLimit: boolean;
} {
  return {
    current: prompt.length,
    max: MAX_PROMPT_LENGTH,
    remaining: MAX_PROMPT_LENGTH - prompt.length,
    isOverLimit: prompt.length > MAX_PROMPT_LENGTH,
  };
}
