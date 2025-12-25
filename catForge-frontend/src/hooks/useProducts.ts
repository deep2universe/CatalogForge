import { useQuery } from '@tanstack/react-query';
import { productsApi } from '@/api';

export function useProducts(category?: string, series?: string) {
  return useQuery({
    queryKey: ['products', { category, series }],
    queryFn: () => productsApi.getAll(category, series),
  });
}

export function useProduct(id: number) {
  return useQuery({
    queryKey: ['products', id],
    queryFn: () => productsApi.getById(id),
    enabled: !!id,
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ['products', 'categories'],
    queryFn: productsApi.getCategories,
  });
}

export function useSeries() {
  return useQuery({
    queryKey: ['products', 'series'],
    queryFn: productsApi.getSeries,
  });
}

export function useProductSearch(query: string) {
  return useQuery({
    queryKey: ['products', 'search', query],
    queryFn: () => productsApi.search(query),
    enabled: query.length >= 2,
  });
}
