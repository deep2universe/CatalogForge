import { forwardRef, type SelectHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';
import { ChevronDown } from 'lucide-react';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  error?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, error, children, ...props }, ref) => {
    return (
      <div className="relative w-full">
        <select
          ref={ref}
          className={cn(
            'w-full px-3 py-2 pr-10 border rounded-lg appearance-none transition-colors',
            'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:border-transparent',
            'bg-white cursor-pointer',
            error ? 'border-pastel-red' : 'border-neutral-200',
            className
          )}
          {...props}
        >
          {children}
        </select>
        <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-400 pointer-events-none" />
        {error && <p className="mt-1 text-sm text-pastel-red-dark">{error}</p>}
      </div>
    );
  }
);

Select.displayName = 'Select';
