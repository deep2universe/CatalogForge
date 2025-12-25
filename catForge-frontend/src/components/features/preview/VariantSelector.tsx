import { cn } from '@/utils/cn';
import type { VariantResponse } from '@/api';

interface VariantSelectorProps {
  variants: VariantResponse[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  disabled?: boolean;
}

export function VariantSelector({
  variants,
  selectedId,
  onSelect,
  disabled = false,
}: VariantSelectorProps) {
  if (variants.length === 0) {
    return (
      <div className="text-center py-4 text-neutral-500">
        Keine Varianten verfügbar
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <h3 className="text-sm font-medium text-neutral-700">
        Varianten ({variants.length})
      </h3>
      <div className="grid grid-cols-2 gap-3">
        {variants.map((variant, index) => (
          <button
            key={variant.id}
            onClick={() => onSelect(variant.id)}
            disabled={disabled}
            className={cn(
              'relative rounded-lg border-2 p-2 transition-all',
              'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:ring-offset-2',
              selectedId === variant.id
                ? 'border-pastel-blue bg-pastel-blue/10'
                : 'border-neutral-200 hover:border-neutral-300',
              disabled && 'opacity-50 cursor-not-allowed'
            )}
          >
            {/* Thumbnail Preview */}
            <div className="aspect-[3/4] bg-neutral-100 rounded overflow-hidden mb-2">
              <VariantThumbnail variant={variant} />
            </div>
            
            {/* Label */}
            <p className="text-xs font-medium text-neutral-700">
              Variante {index + 1}
            </p>
            
            {/* Selected Indicator */}
            {selectedId === variant.id && (
              <div className="absolute top-1 right-1 w-5 h-5 bg-pastel-blue rounded-full flex items-center justify-center">
                <svg
                  className="w-3 h-3 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={3}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
            )}
          </button>
        ))}
      </div>
    </div>
  );
}

// Mini thumbnail component that renders HTML/CSS preview
function VariantThumbnail({ variant }: { variant: VariantResponse }) {
  return (
    <iframe
      srcDoc={`
        <!DOCTYPE html>
        <html>
        <head>
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { 
              font-family: 'Inter', system-ui, sans-serif;
              transform: scale(0.15);
              transform-origin: top left;
              width: 666%;
              height: 666%;
            }
            ${variant.css}
          </style>
        </head>
        <body>${variant.html}</body>
        </html>
      `}
      title="Variant Thumbnail"
      className="w-full h-full border-0 pointer-events-none"
      sandbox="allow-same-origin"
    />
  );
}
