import { useRef, useEffect } from 'react';
import { cn } from '@/utils/cn';
import { getPromptCharacterCount } from '@/utils';

interface PromptInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function PromptInput({
  value,
  onChange,
  placeholder = 'Beschreiben Sie, wie Ihr Katalog aussehen soll...',
  disabled = false,
}: PromptInputProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { current, max, isOverLimit } = getPromptCharacterCount(value);

  // Auto-resize textarea
  useEffect(() => {
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
      textarea.style.height = `${Math.min(textarea.scrollHeight, 200)}px`;
    }
  }, [value]);

  return (
    <div className="space-y-2">
      <textarea
        ref={textareaRef}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        rows={4}
        className={cn(
          'w-full px-4 py-3 border rounded-lg resize-none transition-colors',
          'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:border-transparent',
          'placeholder:text-neutral-400',
          isOverLimit ? 'border-pastel-red' : 'border-neutral-200',
          disabled && 'bg-neutral-50 cursor-not-allowed'
        )}
      />
      <div className="flex justify-end">
        <span
          className={cn(
            'text-sm',
            isOverLimit ? 'text-pastel-red-dark' : 'text-neutral-500'
          )}
        >
          {current.toLocaleString('de-DE')} / {max.toLocaleString('de-DE')} Zeichen
        </span>
      </div>
    </div>
  );
}
