---
priority: 60
dependencies: []
---

## A4 Format Specifications

### Dimensions
- Width: 210mm
- Height: 297mm
- Aspect ratio: 1:1.414

### Safe Margins
- Top: 15mm
- Bottom: 15mm
- Left: 15mm
- Right: 15mm

### Print Bleed
- Add 3mm bleed on all sides for full-bleed printing
- Total with bleed: 216mm x 303mm

### CSS Setup
```css
@page {
  size: A4;
  margin: 15mm;
}

.page {
  width: 210mm;
  height: 297mm;
  box-sizing: border-box;
}
```
