import type { LayoutOptions as LayoutOptionsType, PageFormat, LayoutStyle } from '@/api';
import { Slider, Toggle } from '@/components/ui';
import { cn } from '@/utils/cn';
import { pageFormatDimensions, pageFormats } from '@/utils';

interface LayoutOptionsProps {
  options: LayoutOptionsType;
  onChange: (options: Partial<LayoutOptionsType>) => void;
}

const styles: Array<{ value: LayoutStyle; label: string; color: string }> = [
  { value: 'modern', label: 'Modern', color: 'bg-pastel-blue' },
  { value: 'technical', label: 'Technical', color: 'bg-neutral-200' },
  { value: 'premium', label: 'Premium', color: 'bg-pastel-purple' },
  { value: 'eco', label: 'Eco', color: 'bg-pastel-green' },
  { value: 'dynamic', label: 'Dynamic', color: 'bg-pastel-orange' },
];

export function LayoutOptions({ options, onChange }: LayoutOptionsProps) {
  return (
    <div className="space-y-8">
      {/* Page Format */}
      <div>
        <h3 className="text-sm font-medium text-neutral-800 mb-4">Seitenformat</h3>
        <div className="flex flex-wrap gap-3">
          {pageFormats.map((format) => {
            const isSelected = options.pageFormat === format;
            return (
              <button
                key={format}
                onClick={() => onChange({ pageFormat: format })}
                className={cn(
                  'flex flex-col items-center p-4 rounded-lg border-2 transition-colors min-w-[80px]',
                  isSelected
                    ? 'border-pastel-blue bg-pastel-blue-light'
                    : 'border-neutral-200 hover:border-neutral-300'
                )}
              >
                <FormatIcon format={format} isSelected={isSelected} />
                <span className="mt-2 text-sm font-medium text-neutral-800">
                  {format === 'SQUARE' ? '□' : format}
                </span>
              </button>
            );
          })}
        </div>
        {options.pageFormat && (
          <p className="mt-2 text-sm text-neutral-500">
            {pageFormatDimensions[options.pageFormat].label}
          </p>
        )}
      </div>

      {/* Style */}
      <div>
        <h3 className="text-sm font-medium text-neutral-800 mb-4">Stil</h3>
        <div className="flex flex-wrap gap-3">
          {styles.map((style) => {
            const isSelected = options.style === style.value;
            return (
              <button
                key={style.value}
                onClick={() => onChange({ style: style.value })}
                className={cn(
                  'px-4 py-2 rounded-lg border-2 transition-colors',
                  isSelected
                    ? `border-neutral-800 ${style.color}`
                    : 'border-neutral-200 hover:border-neutral-300'
                )}
              >
                <span className="text-sm font-medium text-neutral-800">
                  {style.label}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Variant Count */}
      <div>
        <h3 className="text-sm font-medium text-neutral-800 mb-4">Varianten</h3>
        <div className="max-w-md">
          <Slider
            min={1}
            max={5}
            value={options.variantCount ?? 2}
            onChange={(e) => onChange({ variantCount: parseInt(e.target.value) })}
            label="Anzahl Varianten"
          />
          <p className="mt-2 text-sm text-neutral-500">
            Aktuell: {options.variantCount ?? 2} Varianten
          </p>
        </div>
      </div>

      {/* Options */}
      <div className="space-y-4">
        <h3 className="text-sm font-medium text-neutral-800">Optionen</h3>
        <div className="space-y-3">
          <Toggle
            checked={options.includeSpecs ?? true}
            onChange={(e) => onChange({ includeSpecs: e.target.checked })}
            label="Technische Daten einbeziehen"
          />
          <Toggle
            checked={options.complexStrategy ?? false}
            onChange={(e) => onChange({ complexStrategy: e.target.checked })}
            label="Komplexe Strategie (mehr LLM-Aufrufe, bessere Qualität)"
          />
        </div>
      </div>
    </div>
  );
}

function FormatIcon({ format, isSelected }: { format: PageFormat; isSelected: boolean }) {
  const dims = pageFormatDimensions[format];
  const aspectRatio = dims.width / dims.height;
  const maxHeight = 40;
  const height = maxHeight;
  const width = height * aspectRatio;

  return (
    <div
      className={cn(
        'border-2 rounded',
        isSelected ? 'border-pastel-blue-dark' : 'border-neutral-300'
      )}
      style={{ width: `${width}px`, height: `${height}px` }}
    />
  );
}
