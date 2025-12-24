---
name: SPACING
category: core
priority: 35
dependencies: [MASTER_SKILL, GRID_SYSTEMS]
---

# Spacing System for Catalog Design

## Spacing Scale

### Base Unit: 8px
- xs: 4px (0.5 units)
- sm: 8px (1 unit)
- md: 16px (2 units)
- lg: 24px (3 units)
- xl: 32px (4 units)
- 2xl: 48px (6 units)
- 3xl: 64px (8 units)

## Component Spacing

### Cards and Containers
- Internal padding: md (16px)
- Card gap: lg (24px)
- Section gap: 2xl (48px)

### Text Elements
- Paragraph spacing: md (16px)
- Heading margin-top: xl (32px)
- Heading margin-bottom: md (16px)

### Lists
- List item gap: sm (8px)
- Nested list indent: lg (24px)

## Print Spacing (mm)

### Margins
- Page margin: 10mm
- Safe area: 5mm from trim
- Bleed extension: 3-5mm

### Element Spacing
- Section gap: 8mm
- Paragraph gap: 4mm
- Line spacing: 1.4-1.6

## Whitespace Principles

### Visual Hierarchy
- More space around important elements
- Group related items with less space
- Use space to create visual breaks

### Breathing Room
- Don't crowd elements
- Allow images to breathe
- Balance density with readability

## CSS Variables
```css
:root {
    --space-xs: 4px;
    --space-sm: 8px;
    --space-md: 16px;
    --space-lg: 24px;
    --space-xl: 32px;
    --space-2xl: 48px;
    --space-3xl: 64px;
}
```
