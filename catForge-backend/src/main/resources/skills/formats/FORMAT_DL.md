---
priority: 60
dependencies: []
---

## DL Format Specifications (Flyer)

### Dimensions
- Width: 99mm
- Height: 210mm
- Aspect ratio: 1:2.12

### Safe Margins
- Top: 8mm
- Bottom: 8mm
- Left: 8mm
- Right: 8mm

### Print Bleed
- Add 3mm bleed on all sides
- Total with bleed: 105mm x 216mm

### CSS Setup
```css
@page {
  size: 99mm 210mm;
  margin: 8mm;
}

.page {
  width: 99mm;
  height: 210mm;
  box-sizing: border-box;
}
```

### Design Notes
- Compact format requires concise content
- Focus on key selling points
- Large, impactful imagery
- Clear call-to-action
