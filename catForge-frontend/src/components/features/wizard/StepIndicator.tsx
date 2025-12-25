import { Check } from 'lucide-react';
import { cn } from '@/utils/cn';

interface Step {
  number: number;
  label: string;
}

interface StepIndicatorProps {
  steps: Step[];
  currentStep: number;
}

export function StepIndicator({ steps, currentStep }: StepIndicatorProps) {
  return (
    <div className="flex items-center justify-center mb-8">
      {steps.map((step, index) => {
        const isCompleted = step.number < currentStep;
        const isCurrent = step.number === currentStep;
        const isLast = index === steps.length - 1;

        return (
          <div key={step.number} className="flex items-center">
            {/* Step Circle */}
            <div className="flex flex-col items-center">
              <div
                className={cn(
                  'w-10 h-10 rounded-full flex items-center justify-center text-sm font-medium transition-colors',
                  isCompleted && 'bg-pastel-green text-neutral-800',
                  isCurrent && 'bg-pastel-blue text-neutral-800',
                  !isCompleted && !isCurrent && 'bg-neutral-200 text-neutral-500'
                )}
              >
                {isCompleted ? <Check className="h-5 w-5" /> : step.number}
              </div>
              <span
                className={cn(
                  'mt-2 text-xs font-medium',
                  isCurrent ? 'text-neutral-800' : 'text-neutral-500'
                )}
              >
                {step.label}
              </span>
            </div>

            {/* Connector Line */}
            {!isLast && (
              <div
                className={cn(
                  'w-16 sm:w-24 h-0.5 mx-2',
                  isCompleted ? 'bg-pastel-green' : 'bg-neutral-200'
                )}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}
