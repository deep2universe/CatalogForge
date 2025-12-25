import { apiClient } from './client';
import type { PdfGenerateRequest, PdfGenerateResponse, PrintPreset } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export const pdfApi = {
  generate: (request: PdfGenerateRequest) =>
    apiClient<PdfGenerateResponse>('/pdf/generate', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  download: async (id: string): Promise<Blob> => {
    const response = await fetch(`${API_BASE_URL}/pdf/${id}/download`);
    if (!response.ok) {
      throw new Error('PDF download failed');
    }
    return response.blob();
  },

  getPresets: () => apiClient<PrintPreset[]>('/pdf/presets'),
};
