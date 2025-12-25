import type { LucideIcon } from 'lucide-react';
import { Card } from '@/components/ui';
import { cn } from '@/utils/cn';

interface StatCardProps {
  title: string;
  value: number | string;
  icon: LucideIcon;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  onClick?: () => void;
  className?: string;
}

export function StatCard({ title, value, icon: Icon, trend, onClick, className }: StatCardProps) {
  return (
    <Card
      hoverable={!!onClick}
      onClick={onClick}
      className={cn('p-6', className)}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-neutral-600">{title}</p>
          <p className="mt-2 text-3xl font-semibold text-neutral-800">{value}</p>
          {trend && (
            <p
              className={cn(
                'mt-1 text-sm',
                trend.isPositive ? 'text-pastel-green-dark' : 'text-pastel-red-dark'
              )}
            >
              {trend.isPositive ? '↑' : '↓'} {Math.abs(trend.value)}%
            </p>
          )}
        </div>
        <div className="p-3 bg-pastel-blue-light rounded-lg">
          <Icon className="h-6 w-6 text-pastel-blue-dark" />
        </div>
      </div>
    </Card>
  );
}
