import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';

interface ToggleProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
}

export const Toggle = forwardRef<HTMLInputElement, ToggleProps>(
  ({ className, label, checked, ...props }, ref) => {
    return (
      <label className={cn('inline-flex items-center cursor-pointer', className)}>
        <div className="relative">
          <input
            ref={ref}
            type="checkbox"
            checked={checked}
            className="sr-only peer"
            {...props}
          />
          <div
            className={cn(
              'w-11 h-6 rounded-full transition-colors',
              'peer-focus:ring-2 peer-focus:ring-pastel-blue peer-focus:ring-offset-2',
              checked ? 'bg-pastel-blue' : 'bg-neutral-200'
            )}
          />
          <div
            className={cn(
              'absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform',
              checked && 'translate-x-5'
            )}
          />
        </div>
        {label && <span className="ml-3 text-sm text-neutral-800">{label}</span>}
      </label>
    );
  }
);

Toggle.displayName = 'Toggle';
