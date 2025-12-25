import { Select } from '@/components/ui';

interface ProductFilterProps {
  categories: string[];
  series: string[];
  selectedCategory: string;
  selectedSeries: string;
  onCategoryChange: (category: string) => void;
  onSeriesChange: (series: string) => void;
}

export function ProductFilter({
  categories,
  series,
  selectedCategory,
  selectedSeries,
  onCategoryChange,
  onSeriesChange,
}: ProductFilterProps) {
  return (
    <div className="flex flex-wrap gap-4">
      <Select
        value={selectedCategory}
        onChange={(e) => onCategoryChange(e.target.value)}
        className="w-48"
      >
        <option value="">Alle Kategorien</option>
        {categories.map((category) => (
          <option key={category} value={category}>
            {category}
          </option>
        ))}
      </Select>

      <Select
        value={selectedSeries}
        onChange={(e) => onSeriesChange(e.target.value)}
        className="w-48"
      >
        <option value="">Alle Baureihen</option>
        {series.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </Select>
    </div>
  );
}
