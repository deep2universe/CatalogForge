import { useState, useMemo } from 'react';
import { X, Plus } from 'lucide-react';
import type { Product } from '@/api';
import { Card, Button, Spinner } from '@/components/ui';
import { ProductSearch, ProductFilter } from '@/components/features/products';
import { useProducts, useCategories, useSeries } from '@/hooks';
import { filterProducts, formatPrice } from '@/utils';

interface ProductSelectorProps {
  selectedProducts: Product[];
  onAddProduct: (product: Product) => void;
  onRemoveProduct: (productId: number) => void;
}

export function ProductSelector({
  selectedProducts,
  onAddProduct,
  onRemoveProduct,
}: ProductSelectorProps) {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedSeries, setSelectedSeries] = useState('');

  const { data: products, isLoading } = useProducts();
  const { data: categories } = useCategories();
  const { data: series } = useSeries();

  const availableProducts = useMemo(() => {
    if (!products) return [];
    const filtered = filterProducts(products, {
      category: selectedCategory || undefined,
      series: selectedSeries || undefined,
      search: search || undefined,
    });
    // Exclude already selected products
    return filtered.filter(
      (p) => !selectedProducts.some((sp) => sp.id === p.id)
    );
  }, [products, selectedCategory, selectedSeries, search, selectedProducts]);

  const isSelected = (productId: number) =>
    selectedProducts.some((p) => p.id === productId);

  return (
    <div className="space-y-6">
      {/* Selected Products */}
      <div>
        <h3 className="text-sm font-medium text-neutral-800 mb-3">
          Ausgewählt: {selectedProducts.length} Produkte
        </h3>
        {selectedProducts.length > 0 ? (
          <div className="flex flex-wrap gap-3">
            {selectedProducts.map((product) => (
              <div
                key={product.id}
                className="flex items-center gap-2 bg-pastel-blue-light px-3 py-2 rounded-lg"
              >
                <div>
                  <p className="text-sm font-medium text-neutral-800">
                    {product.name}
                  </p>
                  <p className="text-xs text-neutral-600">
                    {product.category} · {formatPrice(product.priceEur)}
                  </p>
                </div>
                <button
                  onClick={() => onRemoveProduct(product.id)}
                  className="p-1 hover:bg-pastel-blue rounded transition-colors"
                  aria-label={`${product.name} entfernen`}
                >
                  <X className="h-4 w-4 text-neutral-600" />
                </button>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-neutral-500">
            Noch keine Produkte ausgewählt.
          </p>
        )}
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <ProductSearch value={search} onChange={setSearch} placeholder="Produkt suchen..." />
        </div>
        <ProductFilter
          categories={categories ?? []}
          series={series ?? []}
          selectedCategory={selectedCategory}
          selectedSeries={selectedSeries}
          onCategoryChange={setSelectedCategory}
          onSeriesChange={setSelectedSeries}
        />
      </div>

      {/* Available Products */}
      <div>
        <h3 className="text-sm font-medium text-neutral-800 mb-3">
          Verfügbare Produkte
        </h3>
        {isLoading ? (
          <div className="flex justify-center py-8">
            <Spinner />
          </div>
        ) : availableProducts.length === 0 ? (
          <p className="text-sm text-neutral-500 text-center py-8">
            Keine weiteren Produkte verfügbar.
          </p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
            {availableProducts.map((product) => (
              <Card key={product.id} className="p-3">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-neutral-800 text-sm truncate">
                      {product.name}
                    </p>
                    <p className="text-xs text-neutral-500 mt-0.5">
                      {product.series}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onAddProduct(product)}
                    disabled={isSelected(product.id)}
                    className="flex-shrink-0"
                  >
                    <Plus className="h-4 w-4" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
