---
name: FORMAT_SQUARE
category: formats
priority: 60
dependencies: [MASTER_SKILL, GRID_SYSTEMS]
---

# Square Format Guidelines (210mm × 210mm)

## Page Dimensions

### Size
- Width: 210mm
- Height: 210mm
- Aspect ratio: 1:1

### Margins
- All sides: 15mm
- Safe area: 5mm from trim

### Content Area
- Usable: 180mm × 180mm

## Grid System

### Columns
- 3-column or 4-column grid
- Modular grid works well
- Symmetrical layouts

### Baseline
- 8mm baseline grid
- Balanced spacing

## Typography Scale

### Headlines
- H1: 32-40pt
- H2: 22-28pt
- H3: 16-18pt

### Body Text
- Body: 11-12pt
- Captions: 9-10pt

## Use Cases

### Ideal For
- Social media print materials
- Modern brochures
- Product showcases
- Lookbooks
- Instagram-style layouts

### Design Approach
- Centered compositions
- Symmetrical balance
- Strong visual focus

## Layout Patterns

### Centered
- Central focal point
- Radial balance
- Equal margins

### Quadrant
- 4 equal sections
- Grid-based content
- Modular approach

### Diagonal
- Dynamic tension
- Corner-to-corner flow
- Modern feel

## CSS Example
```css
@page {
    size: 210mm 210mm;
    margin: 15mm;
}

.square-layout {
    width: 180mm;
    height: 180mm;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10mm;
}

.square-centered {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
}

.square-headline {
    font-size: 36pt;
    font-weight: 700;
    margin-bottom: 10mm;
}
```
