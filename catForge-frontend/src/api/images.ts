import { apiClient } from './client';
import type { ImageUploadResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

export const imagesApi = {
  upload: async (file: File): Promise<ImageUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/images/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Image upload failed');
    }

    return response.json();
  },

  uploadBase64: (base64Data: string, mimeType: string, filename?: string) =>
    apiClient<ImageUploadResponse>('/images/upload/base64', {
      method: 'POST',
      body: JSON.stringify({ base64Data, mimeType, filename }),
    }),
};
