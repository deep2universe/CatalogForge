import { useState } from 'react';
import { Button, Spinner } from '@/components/ui';
import { cn } from '@/utils/cn';
import { useGeneratePdf, usePrintPresets } from '@/hooks/usePdf';
import type { PrintPreset } from '@/api';

interface PdfExportProps {
  layoutId: string;
  variantId?: string;
  disabled?: boolean;
}

export function PdfExport({ layoutId, variantId, disabled = false }: PdfExportProps) {
  const [selectedPreset, setSelectedPreset] = useState<string>('standard');
  const { data: presets, isLoading: presetsLoading } = usePrintPresets();
  const generatePdf = useGeneratePdf();

  const handleExport = async () => {
    try {
      const result = await generatePdf.mutateAsync({
        layoutId,
        variantId,
        preset: selectedPreset,
      });
      
      // Open download URL in new tab
      window.open(result.downloadUrl, '_blank');
    } catch (err) {
      console.error('PDF export failed:', err);
    }
  };

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-medium text-neutral-800">PDF Export</h3>
      
      {/* Preset Selection */}
      <div className="space-y-2">
        <label className="text-sm text-neutral-600">Druckprofil</label>
        {presetsLoading ? (
          <div className="flex justify-center py-4">
            <Spinner size="sm" />
          </div>
        ) : (
          <div className="space-y-2">
            {(presets || defaultPresets).map((preset) => (
              <PresetOption
                key={preset.name}
                preset={preset}
                selected={selectedPreset === preset.name}
                onSelect={() => setSelectedPreset(preset.name)}
              />
            ))}
          </div>
        )}
      </div>

      {/* Export Button */}
      <Button
        onClick={handleExport}
        disabled={disabled || generatePdf.isPending}
        isLoading={generatePdf.isPending}
        className="w-full"
      >
        {generatePdf.isPending ? 'Exportiere...' : 'Als PDF exportieren'}
      </Button>

      {generatePdf.isError && (
        <p className="text-sm text-pastel-red-dark">
          Export fehlgeschlagen. Bitte versuchen Sie es erneut.
        </p>
      )}
    </div>
  );
}

interface PresetOptionProps {
  preset: PrintPreset;
  selected: boolean;
  onSelect: () => void;
}

function PresetOption({ preset, selected, onSelect }: PresetOptionProps) {
  return (
    <button
      onClick={onSelect}
      className={cn(
        'w-full text-left p-3 rounded-lg border-2 transition-colors',
        selected
          ? 'border-pastel-blue bg-pastel-blue-light'
          : 'border-neutral-200 hover:border-neutral-300'
      )}
    >
      <p className="text-sm font-medium text-neutral-800">{preset.name}</p>
      <p className="text-xs text-neutral-500 mt-1">{preset.description}</p>
      <div className="flex gap-3 mt-2 text-xs text-neutral-500">
        <span>{preset.dpi} DPI</span>
        {preset.bleedMm > 0 && <span>{preset.bleedMm}mm Beschnitt</span>}
        {preset.cropMarks && <span>Schnittmarken</span>}
      </div>
    </button>
  );
}

const defaultPresets: PrintPreset[] = [
  {
    name: 'standard',
    description: 'Für digitale Nutzung und Bürodrucker',
    dpi: 150,
    bleedMm: 0,
    cropMarks: false,
  },
  {
    name: 'professional',
    description: 'Für professionellen Druck mit Beschnitt',
    dpi: 300,
    bleedMm: 3,
    cropMarks: true,
  },
  {
    name: 'offset',
    description: 'Für Offset-Druck mit erweiterten Einstellungen',
    dpi: 300,
    bleedMm: 5,
    cropMarks: true,
  },
];
