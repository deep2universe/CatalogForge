import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/utils/cn';

interface SliderProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  showValue?: boolean;
}

export const Slider = forwardRef<HTMLInputElement, SliderProps>(
  ({ className, label, showValue = true, value, min = 1, max = 5, ...props }, ref) => {
    return (
      <div className="w-full">
        {(label || showValue) && (
          <div className="flex justify-between mb-2">
            {label && <span className="text-sm text-neutral-600">{label}</span>}
            {showValue && (
              <span className="text-sm font-medium text-neutral-800">{value}</span>
            )}
          </div>
        )}
        <input
          ref={ref}
          type="range"
          min={min}
          max={max}
          value={value}
          className={cn(
            'w-full h-2 bg-neutral-200 rounded-lg appearance-none cursor-pointer',
            'accent-pastel-blue',
            className
          )}
          {...props}
        />
        <div className="flex justify-between mt-1">
          <span className="text-xs text-neutral-400">{min}</span>
          <span className="text-xs text-neutral-400">{max}</span>
        </div>
      </div>
    );
  }
);

Slider.displayName = 'Slider';
