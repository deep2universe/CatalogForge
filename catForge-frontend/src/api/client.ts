import { ApiError } from './errors';
import type { ErrorResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export async function apiClient<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    let errorResponse: ErrorResponse;
    try {
      errorResponse = await response.json();
    } catch {
      errorResponse = {
        timestamp: new Date().toISOString(),
        status: response.status,
        error: response.statusText,
        message: 'Ein unerwarteter Fehler ist aufgetreten.',
        path: endpoint,
      };
    }
    throw new ApiError(errorResponse);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}
