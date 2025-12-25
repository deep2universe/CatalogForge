import { apiClient } from './client';
import type {
  TextToLayoutRequest,
  ImageToLayoutRequest,
  LayoutResponse,
  VariantResponse,
} from './types';

export const layoutsApi = {
  generateFromText: (request: TextToLayoutRequest) =>
    apiClient<LayoutResponse>('/layouts/generate/text', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  generateFromImage: (request: ImageToLayoutRequest) =>
    apiClient<LayoutResponse>('/layouts/generate/image', {
      method: 'POST',
      body: JSON.stringify(request),
    }),

  getById: (id: string) => apiClient<LayoutResponse>(`/layouts/${id}`),

  update: (id: string, layout: Partial<LayoutResponse>) =>
    apiClient<LayoutResponse>(`/layouts/${id}`, {
      method: 'PUT',
      body: JSON.stringify(layout),
    }),

  delete: (id: string) =>
    apiClient<void>(`/layouts/${id}`, { method: 'DELETE' }),

  getVariants: (id: string) =>
    apiClient<VariantResponse[]>(`/layouts/${id}/variants`),
};
