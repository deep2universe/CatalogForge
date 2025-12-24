---
name: GRID_SYSTEMS
category: core
priority: 25
dependencies: [MASTER_SKILL, LAYOUT_PRINCIPLES]
---

# Grid Systems for Catalog Layouts

## 12-Column Grid

### Structure
- 12 equal columns with consistent gutters
- Gutter width: 16px (screen) / 5mm (print)
- Margin: 24px (screen) / 10mm (print)

### Column Spans
- Full width: 12 columns
- Half width: 6 columns
- Third width: 4 columns
- Quarter width: 3 columns

## Modular Grid

### For Product Catalogs
- Use modular grid for product card layouts
- Consistent module size for visual rhythm
- Allow flexible content within modules

### Module Sizing
- Small module: 80x80mm
- Medium module: 120x120mm
- Large module: 160x160mm

## Baseline Grid

### Typography Alignment
- Base unit: 8px
- Line height multiples of base unit
- Vertical spacing in base unit increments

### Implementation
```css
.baseline-grid {
    line-height: 1.5; /* 24px at 16px base */
    margin-bottom: 24px;
}
```

## Print-Specific Grids

### Bleed Area
- Extend backgrounds to bleed edge
- Keep text within safe margin
- Safe margin: 5mm from trim

### Fold Considerations
- Account for fold lines in multi-page layouts
- Avoid critical content near folds
- Consider gutter width for bound documents
