import type { Product } from '@/api';

export interface CategoryCount {
  name: string;
  value: number;
}

export interface SeriesCount {
  name: string;
  value: number;
}

export interface PriceRange {
  min: number;
  max: number;
  avg: number;
}

export function countByCategory(products: Product[]): CategoryCount[] {
  const counts = products.reduce((acc, product) => {
    acc[product.category] = (acc[product.category] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  return Object.entries(counts)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value);
}

export function countBySeries(products: Product[]): SeriesCount[] {
  const counts = products.reduce((acc, product) => {
    acc[product.series] = (acc[product.series] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  return Object.entries(counts)
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value);
}

export function countElectric(products: Product[]): number {
  return products.filter(
    (p) => p.category === 'Elektro' || p.name.toLowerCase().includes('eactros')
  ).length;
}

export function calculatePriceRange(products: Product[]): PriceRange | null {
  const prices = products
    .map((p) => p.priceEur)
    .filter((p): p is number => p !== null);

  if (prices.length === 0) return null;

  return {
    min: Math.min(...prices),
    max: Math.max(...prices),
    avg: prices.reduce((sum, p) => sum + p, 0) / prices.length,
  };
}

export function getTotalCount(products: Product[]): number {
  return products.length;
}

export function getUniqueCategories(products: Product[]): string[] {
  return [...new Set(products.map((p) => p.category))].sort();
}

export function getUniqueSeries(products: Product[]): string[] {
  return [...new Set(products.map((p) => p.series))].sort();
}
