import { cn } from '@/utils/cn';

interface CategoryFilterProps {
  categories: string[];
  selected: string;
  onChange: (category: string) => void;
}

export function CategoryFilter({ categories, selected, onChange }: CategoryFilterProps) {
  const allCategories = ['all', ...categories];

  const labels: Record<string, string> = {
    all: 'Alle Skills',
    core: 'Kern-Skills',
    styles: 'Stil-Skills',
    formats: 'Format-Skills',
  };

  return (
    <div className="space-y-1">
      {allCategories.map((category) => (
        <button
          key={category}
          onClick={() => onChange(category)}
          className={cn(
            'w-full text-left px-3 py-2 rounded-lg text-sm transition-colors',
            selected === category
              ? 'bg-pastel-blue text-neutral-800 font-medium'
              : 'text-neutral-600 hover:bg-neutral-100'
          )}
        >
          {labels[category] || category}
        </button>
      ))}
    </div>
  );
}
