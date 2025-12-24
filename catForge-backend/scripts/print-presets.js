/**
 * Print presets for PDF generation.
 * Defines quality settings for different output purposes.
 */

const PRESETS = {
    'screen': {
        name: 'screen',
        description: 'Screen viewing',
        dpi: 72,
        bleedMm: 0,
        cropMarks: false,
        printBackground: true,
        preferCSSPageSize: true
    },
    'print-standard': {
        name: 'print-standard',
        description: 'Standard office printing',
        dpi: 150,
        bleedMm: 0,
        cropMarks: false,
        printBackground: true,
        preferCSSPageSize: true
    },
    'print-professional': {
        name: 'print-professional',
        description: 'Professional printing',
        dpi: 300,
        bleedMm: 3,
        cropMarks: true,
        printBackground: true,
        preferCSSPageSize: true
    },
    'print-premium': {
        name: 'print-premium',
        description: 'Premium printing with bleed',
        dpi: 300,
        bleedMm: 5,
        cropMarks: true,
        printBackground: true,
        preferCSSPageSize: true
    }
};

/**
 * Page format dimensions in mm.
 */
const PAGE_FORMATS = {
    'A4': { width: 210, height: 297 },
    'A5': { width: 148, height: 210 },
    'A6': { width: 105, height: 148 },
    'DL': { width: 99, height: 210 },
    'square': { width: 210, height: 210 },
    'letter': { width: 216, height: 279 }
};

/**
 * Gets a preset by name.
 * @param {string} name - Preset name
 * @returns {object} Preset configuration
 */
function getPreset(name) {
    return PRESETS[name] || PRESETS['screen'];
}

/**
 * Gets page format dimensions.
 * @param {string} format - Format name
 * @returns {object} Width and height in mm
 */
function getPageFormat(format) {
    return PAGE_FORMATS[format] || PAGE_FORMATS['A4'];
}

/**
 * Calculates page dimensions with bleed.
 * @param {string} format - Format name
 * @param {number} bleedMm - Bleed in mm
 * @returns {object} Total width and height including bleed
 */
function getPageWithBleed(format, bleedMm) {
    const base = getPageFormat(format);
    return {
        width: base.width + (bleedMm * 2),
        height: base.height + (bleedMm * 2)
    };
}

module.exports = {
    PRESETS,
    PAGE_FORMATS,
    getPreset,
    getPageFormat,
    getPageWithBleed
};
