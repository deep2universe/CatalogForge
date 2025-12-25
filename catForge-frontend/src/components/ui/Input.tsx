import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, ...props }, ref) => {
    return (
      <div className="w-full">
        <input
          ref={ref}
          className={cn(
            'w-full px-3 py-2 border rounded-lg transition-colors',
            'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:border-transparent',
            'placeholder:text-neutral-400',
            error ? 'border-pastel-red' : 'border-neutral-200',
            className
          )}
          {...props}
        />
        {error && <p className="mt-1 text-sm text-pastel-red-dark">{error}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
