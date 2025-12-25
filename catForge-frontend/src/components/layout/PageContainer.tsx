import type { ReactNode } from 'react';
import { cn } from '@/utils/cn';

interface PageContainerProps {
  children: ReactNode;
  className?: string;
}

export function PageContainer({ children, className }: PageContainerProps) {
  return (
    <div className={cn('px-6 py-6 max-w-7xl mx-auto', className)}>
      {children}
    </div>
  );
}
