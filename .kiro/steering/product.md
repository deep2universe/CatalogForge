# CatalogForge

AI-powered catalog and flyer generation platform that transforms text prompts and reference images into professional, print-ready HTML/CSS layouts and PDFs.

## Core Functionality

- **Text-to-Layout**: Generate HTML/CSS layouts from natural language prompts
- **Image-to-Layout**: Analyze reference images via Gemini Vision for color palettes, mood, and layout hints
- **Skills System**: Modular prompt engineering with reusable markdown-based skills
- **Agent Framework**: Flexible pipelines (Linear, Iterative, Parallel) for layout generation
- **PDF Export**: Print-ready PDFs via Puppeteer with professional presets (bleed, crop marks)
- **Multi-Variant Generation**: Parallel generation of multiple layout variants

## Key Design Decisions

- **HTML/CSS as layout language**: Web technologies for maximum flexibility, browser-based rendering
- **LLM-first image processing**: Gemini Vision for semantic understanding instead of traditional CV libraries
- **Skills-based constraints**: Structured prompts that constrain LLM output to valid, print-ready layouts
- **No database**: JSON files for product data, in-memory layout storage (MVP scope)
- **URL-based image handling**: No server-side image storage, GDPR-friendly
- **Custom agent framework**: Full control, no LangChain/LlamaIndex dependencies

## Domain Context

- Target: B2B automotive catalogs (Daimler Truck products)
- Output formats: A4, A5, DL, A6, Square
- Styles: Modern, Technical, Premium, Eco, Dynamic
- Language: German (documentation and prompts)
