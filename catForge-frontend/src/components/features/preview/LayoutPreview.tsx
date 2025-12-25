import { useState, useRef, useEffect } from 'react';
import { cn } from '@/utils/cn';
import { Button } from '@/components/ui';
import type { VariantResponse } from '@/api';

interface LayoutPreviewProps {
  variant: VariantResponse | null;
  pageFormat?: string;
  className?: string;
}

const ZOOM_LEVELS = [50, 75, 100, 125, 150];

export function LayoutPreview({ variant, pageFormat = 'A4', className }: LayoutPreviewProps) {
  const [zoom, setZoom] = useState(100);
  const iframeRef = useRef<HTMLIFrameElement>(null);

  // Update iframe content when variant changes
  useEffect(() => {
    if (iframeRef.current && variant) {
      const doc = iframeRef.current.contentDocument;
      if (doc) {
        doc.open();
        doc.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <style>
              * { margin: 0; padding: 0; box-sizing: border-box; }
              body { 
                font-family: 'Inter', system-ui, sans-serif;
                background: white;
              }
              ${variant.css}
            </style>
          </head>
          <body>
            ${variant.html}
          </body>
          </html>
        `);
        doc.close();
      }
    }
  }, [variant]);

  const handleZoomIn = () => {
    const currentIndex = ZOOM_LEVELS.indexOf(zoom);
    if (currentIndex < ZOOM_LEVELS.length - 1) {
      setZoom(ZOOM_LEVELS[currentIndex + 1]);
    }
  };

  const handleZoomOut = () => {
    const currentIndex = ZOOM_LEVELS.indexOf(zoom);
    if (currentIndex > 0) {
      setZoom(ZOOM_LEVELS[currentIndex - 1]);
    }
  };

  const handleResetZoom = () => setZoom(100);

  // Get aspect ratio based on page format
  const getAspectRatio = () => {
    switch (pageFormat) {
      case 'A4': return 'aspect-[210/297]';
      case 'A5': return 'aspect-[148/210]';
      case 'A6': return 'aspect-[105/148]';
      case 'DL': return 'aspect-[99/210]';
      case 'SQUARE': return 'aspect-square';
      default: return 'aspect-[210/297]';
    }
  };

  if (!variant) {
    return (
      <div className={cn('flex items-center justify-center bg-neutral-100 rounded-lg p-8', className)}>
        <p className="text-neutral-500">Kein Layout ausgewählt</p>
      </div>
    );
  }

  return (
    <div className={cn('flex flex-col', className)}>
      {/* Zoom Controls */}
      <div className="flex items-center justify-between mb-4 px-2">
        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={handleZoomOut}
            disabled={zoom === ZOOM_LEVELS[0]}
          >
            −
          </Button>
          <span className="text-sm text-neutral-600 min-w-[4rem] text-center">
            {zoom}%
          </span>
          <Button
            variant="secondary"
            size="sm"
            onClick={handleZoomIn}
            disabled={zoom === ZOOM_LEVELS[ZOOM_LEVELS.length - 1]}
          >
            +
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={handleResetZoom}
          >
            Zurücksetzen
          </Button>
        </div>
        <span className="text-sm text-neutral-500">
          Format: {pageFormat}
        </span>
      </div>

      {/* Preview Container */}
      <div className="flex-1 overflow-auto bg-neutral-100 rounded-lg p-4">
        <div
          className="mx-auto transition-transform origin-top"
          style={{ transform: `scale(${zoom / 100})` }}
        >
          <div className={cn('bg-white shadow-lg mx-auto', getAspectRatio(), 'w-[210mm] max-w-full')}>
            <iframe
              ref={iframeRef}
              title="Layout Preview"
              className="w-full h-full border-0"
              sandbox="allow-same-origin"
            />
          </div>
        </div>
      </div>
    </div>
  );
}
