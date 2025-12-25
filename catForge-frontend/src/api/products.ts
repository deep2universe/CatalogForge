import { apiClient } from './client';
import type { Product } from './types';

export const productsApi = {
  getAll: (category?: string, series?: string) => {
    const params = new URLSearchParams();
    if (category) params.set('category', category);
    if (series) params.set('series', series);
    const query = params.toString();
    return apiClient<Product[]>(`/products${query ? `?${query}` : ''}`);
  },

  getById: (id: number) => apiClient<Product>(`/products/${id}`),

  getCategories: () => apiClient<string[]>('/products/categories'),

  getSeries: () => apiClient<string[]>('/products/series'),

  search: (query: string) =>
    apiClient<Product[]>(`/products/search?q=${encodeURIComponent(query)}`),
};
