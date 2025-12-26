---
inclusion: always
---

# CatalogForge

AI-powered catalog and flyer generation platform that transforms text prompts and reference images into professional, print-ready HTML/CSS layouts and PDFs.

## Core Functionality

- **Text-to-Layout**: Generate HTML/CSS layouts from natural language prompts
- **Image-to-Layout**: Analyze reference images via Gemini Vision for color palettes, mood, and layout hints
- **Skills System**: Modular prompt engineering with reusable markdown-based skills (located in `src/main/resources/skills/`)
- **Agent Framework**: Flexible pipelines (Linear, Iterative, Parallel) for layout generation
- **PDF Export**: Print-ready PDFs via Puppeteer with professional presets (bleed, crop marks)
- **Multi-Variant Generation**: Parallel generation of multiple layout variants

## Architecture Principles

- **HTML/CSS as layout language**: Web technologies for maximum flexibility, browser-based rendering
- **LLM-first image processing**: Gemini Vision for semantic understanding instead of traditional CV
- **Skills-based constraints**: Structured prompts that constrain LLM output to valid, print-ready layouts
- **No database**: JSON files for product data (`src/main/resources/data/products.json`), in-memory layout storage
- **URL-based image handling**: No server-side image storage, GDPR-friendly
- **Custom agent framework**: Full control, no LangChain/LlamaIndex dependencies

## Domain Context

- **Target audience**: B2B automotive catalogs (Daimler Truck products)
- **Output formats**: A4, A5, DL, A6, Square (defined in `skills/formats/`)
- **Visual styles**: Modern, Technical, Premium, Eco, Dynamic (defined in `skills/styles/`)
- **Language**: German for user-facing content and prompts

## Code Guidelines

When modifying this codebase:

1. **Generated layouts must be print-ready**: Always include proper margins, bleed areas, and safe zones
2. **Skills are composable**: Combine core skills with format and style skills for complete prompts
3. **Layouts are immutable**: Use `withX()` methods in AgentContext for state changes
4. **Validate CSS output**: Use `CssValidator` to ensure generated CSS is valid
5. **Sanitize HTML output**: Use `HtmlSanitizer` before rendering user-influenced content
6. **Log LLM interactions**: All Gemini API calls should be logged via `LlmInteractionLogger`
