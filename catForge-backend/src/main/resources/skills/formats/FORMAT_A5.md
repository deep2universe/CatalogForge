---
name: FORMAT_A5
category: formats
priority: 60
dependencies: [MASTER_SKILL, GRID_SYSTEMS]
---

# A5 Format Guidelines (148mm × 210mm)

## Page Dimensions

### Size
- Width: 148mm
- Height: 210mm
- Aspect ratio: ~1:1.41

### Margins
- Top: 12mm
- Bottom: 15mm
- Inside: 15mm (for binding)
- Outside: 12mm

### Safe Area
- 5mm from trim edge
- Content area: 126mm × 185mm

## Grid System

### Columns
- 2-column layout recommended
- Column width: ~58mm each
- Gutter: 10mm

### Baseline
- 6mm baseline grid
- Line height: 14-16pt

## Typography Scale

### Headlines
- H1: 24-28pt
- H2: 18-22pt
- H3: 14-16pt

### Body Text
- Body: 10-11pt
- Captions: 8-9pt
- Footnotes: 7-8pt

## Use Cases

### Ideal For
- Compact product brochures
- Quick reference guides
- Pocket catalogs
- Handouts

### Content Density
- Medium density
- Focus on key information
- Concise descriptions

## CSS Example
```css
@page {
    size: A5;
    margin: 12mm 12mm 15mm 15mm;
}

.a5-layout {
    max-width: 126mm;
    font-size: 10pt;
    line-height: 1.5;
}

.a5-headline {
    font-size: 24pt;
    margin-bottom: 8mm;
}
```
