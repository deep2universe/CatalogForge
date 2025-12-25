import type { Product, Skill, ExamplePrompt, LayoutStyle, PageFormat } from '@/api';

export type SortDirection = 'asc' | 'desc';

export interface ProductFilters {
  category?: string;
  series?: string;
  search?: string;
}

export function filterProducts(products: Product[], filters: ProductFilters): Product[] {
  return products.filter((product) => {
    if (filters.category && product.category !== filters.category) return false;
    if (filters.series && product.series !== filters.series) return false;
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      const matchesName = product.name.toLowerCase().includes(searchLower);
      const matchesDescription = product.shortDescription.toLowerCase().includes(searchLower);
      if (!matchesName && !matchesDescription) return false;
    }
    return true;
  });
}

export function sortProducts(
  products: Product[],
  sortBy: 'name' | 'price' | 'category' | 'series',
  direction: SortDirection = 'asc'
): Product[] {
  const sorted = [...products].sort((a, b) => {
    let comparison = 0;
    switch (sortBy) {
      case 'name':
        comparison = a.name.localeCompare(b.name, 'de');
        break;
      case 'price':
        const priceA = a.priceEur ?? 0;
        const priceB = b.priceEur ?? 0;
        comparison = priceA - priceB;
        break;
      case 'category':
        comparison = a.category.localeCompare(b.category, 'de');
        break;
      case 'series':
        comparison = a.series.localeCompare(b.series, 'de');
        break;
    }
    return direction === 'asc' ? comparison : -comparison;
  });
  return sorted;
}

export function filterSkills(skills: Skill[], category?: string): Skill[] {
  if (!category || category === 'all') return skills;
  return skills.filter((skill) => skill.category === category);
}

export function sortSkills(skills: Skill[]): Skill[] {
  return [...skills].sort((a, b) => a.priority - b.priority);
}

export function filterPrompts(
  prompts: ExamplePrompt[],
  style?: LayoutStyle | 'all',
  format?: PageFormat | 'all'
): ExamplePrompt[] {
  return prompts.filter((prompt) => {
    if (style && style !== 'all' && prompt.style !== style) return false;
    if (format && format !== 'all' && prompt.format !== format) return false;
    return true;
  });
}

export function groupProductsByCategory(products: Product[]): Record<string, Product[]> {
  return products.reduce((acc, product) => {
    if (!acc[product.category]) {
      acc[product.category] = [];
    }
    acc[product.category].push(product);
    return acc;
  }, {} as Record<string, Product[]>);
}

export function groupProductsBySeries(products: Product[]): Record<string, Product[]> {
  return products.reduce((acc, product) => {
    if (!acc[product.series]) {
      acc[product.series] = [];
    }
    acc[product.series].push(product);
    return acc;
  }, {} as Record<string, Product[]>);
}
