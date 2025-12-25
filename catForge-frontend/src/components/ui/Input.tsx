import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  label?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, label, id, ...props }, ref) => {
    const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);
    const errorId = error ? `${inputId}-error` : undefined;

    return (
      <div className="w-full">
        {label && (
          <label htmlFor={inputId} className="block text-sm font-medium text-neutral-700 mb-1">
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={cn(
            'w-full px-3 py-2 border rounded-lg transition-colors',
            'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:border-transparent',
            'placeholder:text-neutral-400',
            error ? 'border-pastel-red' : 'border-neutral-200',
            className
          )}
          aria-invalid={!!error}
          aria-describedby={errorId}
          {...props}
        />
        {error && (
          <p id={errorId} className="mt-1 text-sm text-pastel-red-dark" role="alert">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
