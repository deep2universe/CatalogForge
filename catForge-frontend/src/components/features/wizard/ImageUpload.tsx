import { useRef, useState, useCallback } from 'react';
import { cn } from '@/utils/cn';
import { validateImage, ALLOWED_IMAGE_TYPES, MAX_FILE_SIZE } from '@/utils';
import { Button } from '@/components/ui';

interface ImageUploadProps {
  value: { base64: string; mimeType: string } | null;
  onChange: (image: { base64: string; mimeType: string } | null) => void;
  disabled?: boolean;
}

export function ImageUpload({ value, onChange, disabled = false }: ImageUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const processFile = useCallback(async (file: File) => {
    setError(null);
    const validation = validateImage(file);
    if (!validation.valid) {
      setError(validation.message || 'Ungültige Datei');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const base64 = (reader.result as string).split(',')[1];
      onChange({ base64, mimeType: file.type });
    };
    reader.onerror = () => setError('Fehler beim Lesen der Datei');
    reader.readAsDataURL(file);
  }, [onChange]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) processFile(file);
  };

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (disabled) return;
    const file = e.dataTransfer.files[0];
    if (file) processFile(file);
  }, [disabled, processFile]);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    if (!disabled) setIsDragging(true);
  };

  const handleDragLeave = () => setIsDragging(false);

  const handleRemove = () => {
    onChange(null);
    setError(null);
    if (inputRef.current) inputRef.current.value = '';
  };

  return (
    <div className="space-y-2">
      <input
        ref={inputRef}
        type="file"
        accept={ALLOWED_IMAGE_TYPES.join(',')}
        onChange={handleFileChange}
        disabled={disabled}
        className="hidden"
      />

      {value ? (
        <div className="relative rounded-lg border border-neutral-200 p-4">
          <div className="flex items-center gap-4">
            <img
              src={`data:${value.mimeType};base64,${value.base64}`}
              alt="Referenzbild"
              className="h-20 w-20 rounded-lg object-cover"
            />
            <div className="flex-1">
              <p className="text-sm font-medium text-neutral-700">Referenzbild hochgeladen</p>
              <p className="text-xs text-neutral-500">
                Das Bild wird für die Stilanalyse verwendet
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleRemove}
              disabled={disabled}
            >
              Entfernen
            </Button>
          </div>
        </div>
      ) : (
        <div
          onClick={() => !disabled && inputRef.current?.click()}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          className={cn(
            'flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-8 transition-colors cursor-pointer',
            isDragging ? 'border-pastel-blue bg-pastel-blue/10' : 'border-neutral-300',
            disabled ? 'cursor-not-allowed opacity-50' : 'hover:border-pastel-blue hover:bg-pastel-blue/5'
          )}
        >
          <svg
            className="mb-3 h-10 w-10 text-neutral-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <p className="text-sm font-medium text-neutral-700">
            Referenzbild hochladen (optional)
          </p>
          <p className="mt-1 text-xs text-neutral-500">
            JPG, PNG oder WebP bis {MAX_FILE_SIZE / 1024 / 1024}MB
          </p>
        </div>
      )}

      {error && (
        <p className="text-sm text-pastel-red-dark">{error}</p>
      )}
    </div>
  );
}
