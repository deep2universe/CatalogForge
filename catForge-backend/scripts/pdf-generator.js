#!/usr/bin/env node

/**
 * PDF Generator using Puppeteer.
 * Reads JSON input from stdin, generates PDF, outputs JSON result to stdout.
 * 
 * Input JSON:
 * {
 *   "html": "<html>...</html>",
 *   "css": "body { ... }",
 *   "preset": "screen|print-standard|print-professional|print-premium",
 *   "pageFormat": "A4|A5|DL|...",
 *   "landscape": false,
 *   "dpi": 300,
 *   "bleedMm": 3,
 *   "cropMarks": true
 * }
 * 
 * Output JSON:
 * {
 *   "success": true,
 *   "pdfPath": "/path/to/output.pdf"
 * }
 */

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { getPreset, getPageFormat, getPageWithBleed } = require('./print-presets');

/**
 * Reads JSON input from stdin.
 */
async function readInput() {
    return new Promise((resolve, reject) => {
        let data = '';
        process.stdin.setEncoding('utf8');
        process.stdin.on('data', chunk => data += chunk);
        process.stdin.on('end', () => {
            try {
                resolve(JSON.parse(data));
            } catch (e) {
                reject(new Error('Invalid JSON input: ' + e.message));
            }
        });
        process.stdin.on('error', reject);
    });
}

/**
 * Generates crop marks SVG.
 */
function generateCropMarks(width, height, bleed) {
    const markLength = 10;
    const markOffset = 3;
    
    return `
        <svg class="crop-marks" style="position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;">
            <!-- Top-left -->
            <line x1="${bleed - markOffset}mm" y1="${bleed}mm" x2="${bleed - markOffset - markLength}mm" y2="${bleed}mm" stroke="black" stroke-width="0.25"/>
            <line x1="${bleed}mm" y1="${bleed - markOffset}mm" x2="${bleed}mm" y2="${bleed - markOffset - markLength}mm" stroke="black" stroke-width="0.25"/>
            
            <!-- Top-right -->
            <line x1="${width - bleed + markOffset}mm" y1="${bleed}mm" x2="${width - bleed + markOffset + markLength}mm" y2="${bleed}mm" stroke="black" stroke-width="0.25"/>
            <line x1="${width - bleed}mm" y1="${bleed - markOffset}mm" x2="${width - bleed}mm" y2="${bleed - markOffset - markLength}mm" stroke="black" stroke-width="0.25"/>
            
            <!-- Bottom-left -->
            <line x1="${bleed - markOffset}mm" y1="${height - bleed}mm" x2="${bleed - markOffset - markLength}mm" y2="${height - bleed}mm" stroke="black" stroke-width="0.25"/>
            <line x1="${bleed}mm" y1="${height - bleed + markOffset}mm" x2="${bleed}mm" y2="${height - bleed + markOffset + markLength}mm" stroke="black" stroke-width="0.25"/>
            
            <!-- Bottom-right -->
            <line x1="${width - bleed + markOffset}mm" y1="${height - bleed}mm" x2="${width - bleed + markOffset + markLength}mm" y2="${height - bleed}mm" stroke="black" stroke-width="0.25"/>
            <line x1="${width - bleed}mm" y1="${height - bleed + markOffset}mm" x2="${width - bleed}mm" y2="${height - bleed + markOffset + markLength}mm" stroke="black" stroke-width="0.25"/>
        </svg>
    `;
}

/**
 * Builds the full HTML document.
 */
function buildHtmlDocument(input) {
    const preset = getPreset(input.preset);
    const bleed = input.bleedMm || preset.bleedMm || 0;
    const pageSize = getPageWithBleed(input.pageFormat, bleed);
    
    let cropMarksHtml = '';
    if (input.cropMarks || preset.cropMarks) {
        cropMarksHtml = generateCropMarks(pageSize.width, pageSize.height, bleed);
    }
    
    return `
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CatalogForge PDF</title>
    <style>
        @page {
            size: ${pageSize.width}mm ${pageSize.height}mm;
            margin: 0;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        html, body {
            width: ${pageSize.width}mm;
            height: ${pageSize.height}mm;
        }
        
        .pdf-container {
            position: relative;
            width: 100%;
            height: 100%;
            padding: ${bleed}mm;
        }
        
        .pdf-content {
            width: 100%;
            height: 100%;
            overflow: hidden;
        }
        
        ${input.css || ''}
    </style>
</head>
<body>
    <div class="pdf-container">
        ${cropMarksHtml}
        <div class="pdf-content">
            ${input.html || ''}
        </div>
    </div>
</body>
</html>
    `;
}

/**
 * Main PDF generation function.
 */
async function generatePdf(input) {
    const preset = getPreset(input.preset);
    const pageSize = getPageWithBleed(input.pageFormat, input.bleedMm || preset.bleedMm || 0);
    
    // Generate output path
    const outputDir = path.join(os.tmpdir(), 'catalogforge-pdf');
    if (!fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, { recursive: true });
    }
    const outputPath = path.join(outputDir, `pdf-${Date.now()}-${Math.random().toString(36).substr(2, 9)}.pdf`);
    
    // Launch browser
    const browser = await puppeteer.launch({
        headless: 'new',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    
    try {
        const page = await browser.newPage();
        
        // Set viewport for high DPI
        const dpi = input.dpi || preset.dpi || 72;
        const scale = dpi / 72;
        
        await page.setViewport({
            width: Math.round(pageSize.width * 3.78 * scale), // mm to px at 96dpi * scale
            height: Math.round(pageSize.height * 3.78 * scale),
            deviceScaleFactor: scale
        });
        
        // Load HTML content
        const htmlContent = buildHtmlDocument(input);
        await page.setContent(htmlContent, { waitUntil: 'networkidle0' });
        
        // Generate PDF
        await page.pdf({
            path: outputPath,
            width: `${pageSize.width}mm`,
            height: `${pageSize.height}mm`,
            printBackground: preset.printBackground,
            preferCSSPageSize: preset.preferCSSPageSize,
            landscape: input.landscape || false,
            margin: { top: 0, right: 0, bottom: 0, left: 0 }
        });
        
        return {
            success: true,
            pdfPath: outputPath,
            preset: preset.name,
            pageFormat: input.pageFormat,
            dimensions: pageSize
        };
        
    } finally {
        await browser.close();
    }
}

/**
 * Main entry point.
 */
async function main() {
    try {
        const input = await readInput();
        const result = await generatePdf(input);
        console.log(JSON.stringify(result));
        process.exit(0);
    } catch (error) {
        console.error(JSON.stringify({
            success: false,
            error: error.message
        }));
        process.exit(1);
    }
}

main();
