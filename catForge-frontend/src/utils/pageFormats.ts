import type { PageFormat } from '@/api';

export interface PageDimensions {
  width: number;
  height: number;
  unit: 'mm';
  label: string;
}

export const pageFormatDimensions: Record<PageFormat, PageDimensions> = {
  A4: { width: 210, height: 297, unit: 'mm', label: 'A4 (210×297mm)' },
  A5: { width: 148, height: 210, unit: 'mm', label: 'A5 (148×210mm)' },
  A6: { width: 105, height: 148, unit: 'mm', label: 'A6 (105×148mm)' },
  DL: { width: 99, height: 210, unit: 'mm', label: 'DL (99×210mm)' },
  SQUARE: { width: 210, height: 210, unit: 'mm', label: 'Quadrat (210×210mm)' },
};

export const pageFormats: PageFormat[] = ['A4', 'A5', 'A6', 'DL', 'SQUARE'];

export function getPageDimensions(format: PageFormat): PageDimensions {
  return pageFormatDimensions[format];
}

export function getAspectRatio(format: PageFormat): number {
  const dims = pageFormatDimensions[format];
  return dims.width / dims.height;
}

export function isPortrait(format: PageFormat): boolean {
  const dims = pageFormatDimensions[format];
  return dims.height > dims.width;
}

export function isLandscape(format: PageFormat): boolean {
  const dims = pageFormatDimensions[format];
  return dims.width > dims.height;
}

export function isSquare(format: PageFormat): boolean {
  const dims = pageFormatDimensions[format];
  return dims.width === dims.height;
}
