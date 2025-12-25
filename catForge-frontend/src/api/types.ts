// Products
export interface Product {
  id: number;
  name: string;
  shortDescription: string;
  description: string;
  longDescription: string;
  category: string;
  series: string;
  specs: Record<string, string>;
  highlights: string[];
  imageUrl: string;
  priceEur: number | null;
}

// Layouts
export type PageFormat = 'A4' | 'A5' | 'A6' | 'DL' | 'SQUARE';
export type LayoutStyle = 'modern' | 'technical' | 'premium' | 'eco' | 'dynamic';

export interface LayoutOptions {
  pageFormat?: PageFormat;
  style?: LayoutStyle;
  variantCount?: number;
  includeSpecs?: boolean;
  complexStrategy?: boolean;
}

export interface TextToLayoutRequest {
  productIds: number[];
  options?: LayoutOptions;
  prompt: string;
}

export interface ImageToLayoutRequest {
  productIds: number[];
  options?: LayoutOptions;
  prompt?: string;
  imageBase64: string;
  imageMimeType: string;
}

export interface VariantResponse {
  id: string;
  html: string;
  css: string;
}

export interface LayoutResponse {
  id: string;
  status: string;
  generatedAt: string;
  pageFormat: string;
  variants: VariantResponse[];
  variantCount: number;
}

// Skills
export interface Skill {
  name: string;
  category: string;
  content: string;
  dependencies: string[];
  priority: number;
}

export interface ExamplePrompt {
  id: string;
  title: string;
  description?: string;
  prompt: string;
  skills: string[];
  style: string;
  format: string;
}

// PDF
export interface PdfGenerateRequest {
  layoutId: string;
  variantId?: string;
  preset?: string;
}

export interface PdfGenerateResponse {
  pdfId: string;
  downloadUrl: string;
}

export interface PrintPreset {
  name: string;
  description: string;
  dpi: number;
  bleedMm: number;
  cropMarks: boolean;
}

// Images
export interface ImageUploadResponse {
  imageId: string;
  url: string;
  mimeType: string;
  expiresAt: string;
}

// Errors
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
