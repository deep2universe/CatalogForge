import { forwardRef, type HTMLAttributes } from 'react';
import { cn } from '@/utils/cn';
import type { LayoutStyle } from '@/api/types';

export type BadgeVariant = 'default' | 'blue' | 'green' | 'purple' | 'yellow' | 'orange' | 'red';

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
}

const variantStyles: Record<BadgeVariant, string> = {
  default: 'bg-neutral-100 text-neutral-600',
  blue: 'bg-pastel-blue-light text-neutral-800',
  green: 'bg-pastel-green-light text-neutral-800',
  purple: 'bg-pastel-purple-light text-neutral-800',
  yellow: 'bg-pastel-yellow-light text-neutral-800',
  orange: 'bg-pastel-orange-light text-neutral-800',
  red: 'bg-pastel-red-light text-neutral-800',
};

export const styleToBadgeVariant: Record<LayoutStyle, BadgeVariant> = {
  modern: 'blue',
  technical: 'default',
  premium: 'purple',
  eco: 'green',
  dynamic: 'orange',
};

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className, variant = 'default', children, ...props }, ref) => {
    return (
      <span
        ref={ref}
        className={cn(
          'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
          variantStyles[variant],
          className
        )}
        {...props}
      >
        {children}
      </span>
    );
  }
);

Badge.displayName = 'Badge';
