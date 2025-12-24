---
name: STYLE_DYNAMIC
category: styles
priority: 50
dependencies: [MASTER_SKILL, TYPOGRAPHY, COLOR_THEORY]
---

# Dynamic Style Guide

## Visual Characteristics

### Overall Aesthetic
- Bold and energetic
- Strong visual impact
- Dynamic angles and shapes
- Action-oriented

### Color Palette
- Primary: Vibrant red (#E63946) or orange (#FF6B35)
- Secondary: Electric blue (#0077B6) or purple (#7B2CBF)
- Accent: Bright yellow (#FFD60A) or lime (#AAFF00)
- Background: Dark (#1A1A2E) or white (#FFFFFF)
- Text: High contrast

## Typography

### Font Choices
- Headlines: Bold, impactful (Montserrat Black, Oswald)
- Body: Strong, readable (Roboto, Source Sans Pro)
- Numbers: Tabular, prominent

### Sizing
- Headlines: Large, commanding (40-56pt)
- Subheads: Strong presence (20-28pt)
- Body: Clear and direct (11-12pt)

## Layout Elements

### Spacing
- Tight, energetic spacing
- Dynamic asymmetry
- Bold section breaks

### Visual Elements
- Diagonal lines and angles
- Bold geometric shapes
- High-contrast borders
- Motion-suggesting elements

### Imagery
- Action shots
- Dynamic angles
- High contrast
- Motion blur effects

## CSS Example
```css
.dynamic-layout {
    font-family: 'Montserrat', sans-serif;
    background: #1A1A2E;
    color: #FFFFFF;
}

.dynamic-headline {
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: #E63946;
}

.dynamic-accent {
    background: linear-gradient(135deg, #E63946, #FF6B35);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.dynamic-card {
    border-left: 4px solid #E63946;
    transform: skewX(-2deg);
}

.dynamic-stat {
    font-size: 48px;
    font-weight: 900;
    color: #FFD60A;
}
```
