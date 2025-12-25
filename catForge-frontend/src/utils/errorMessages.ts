export type ErrorCode = 400 | 404 | 413 | 500 | 502 | 503;

export const errorMessages: Record<number, string> = {
  400: 'Ungültige Eingabe. Bitte überprüfen Sie Ihre Daten.',
  404: 'Die angeforderte Ressource wurde nicht gefunden.',
  413: 'Die Datei ist zu groß. Maximale Größe: 10MB.',
  500: 'Ein unerwarteter Fehler ist aufgetreten.',
  502: 'Der KI-Service ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut.',
  503: 'Der Service ist vorübergehend nicht verfügbar.',
};

export function getErrorMessage(status: number): string {
  return errorMessages[status] || errorMessages[500];
}

export function isKnownErrorCode(status: number): boolean {
  return status in errorMessages;
}
