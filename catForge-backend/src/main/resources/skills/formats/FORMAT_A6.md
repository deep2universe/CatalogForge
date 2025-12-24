---
name: FORMAT_A6
category: formats
priority: 60
dependencies: [MASTER_SKILL, GRID_SYSTEMS]
---

# A6 Format Guidelines (105mm × 148mm)

## Page Dimensions

### Size
- Width: 105mm
- Height: 148mm
- Aspect ratio: ~1:1.41

### Margins
- All sides: 8mm
- Safe area: 5mm from trim

### Content Area
- Usable width: 89mm
- Usable height: 132mm

## Grid System

### Columns
- Single column recommended
- Full width for impact
- Optional 2-column for lists

### Baseline
- 5mm baseline grid
- Compact line spacing

## Typography Scale

### Headlines
- H1: 18-22pt
- H2: 14-16pt
- H3: 11-12pt

### Body Text
- Body: 9-10pt
- Captions: 7-8pt
- Minimum: 7pt

## Use Cases

### Ideal For
- Postcards
- Flyers
- Quick info cards
- Product tags
- Event invitations

### Content Strategy
- Minimal text
- Strong visuals
- Single message focus
- Clear CTA

## Design Tips

### Hierarchy
- One dominant element
- Limited text blocks
- Bold headlines

### Imagery
- Large product image
- Simple backgrounds
- High impact visuals

## CSS Example
```css
@page {
    size: A6;
    margin: 8mm;
}

.a6-layout {
    max-width: 89mm;
    font-size: 9pt;
    line-height: 1.4;
}

.a6-headline {
    font-size: 18pt;
    font-weight: 700;
    margin-bottom: 4mm;
}

.a6-image {
    width: 100%;
    height: auto;
    margin-bottom: 4mm;
}
```
