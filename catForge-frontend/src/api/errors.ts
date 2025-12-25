import type { ErrorResponse } from './types';

export class ApiError extends Error {
  constructor(public response: ErrorResponse) {
    super(response.message);
    this.name = 'ApiError';
  }

  get status() {
    return this.response.status;
  }

  get isNotFound() {
    return this.status === 404;
  }

  get isValidationError() {
    return this.status === 400;
  }

  get isServerError() {
    return this.status >= 500;
  }
}

export const errorMessages: Record<number, string> = {
  400: 'Ungültige Eingabe. Bitte überprüfen Sie Ihre Daten.',
  404: 'Die angeforderte Ressource wurde nicht gefunden.',
  413: 'Die Datei ist zu groß. Maximale Größe: 10MB.',
  502: 'Der KI-Service ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut.',
  500: 'Ein unerwarteter Fehler ist aufgetreten.',
};

export function getErrorMessage(status: number): string {
  return errorMessages[status] || errorMessages[500];
}
