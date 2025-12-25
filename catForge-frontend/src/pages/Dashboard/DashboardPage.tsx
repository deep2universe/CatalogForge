import { useState, useMemo } from 'react';
import { Package, FolderTree, Layers, Zap } from 'lucide-react';
import { PageContainer } from '@/components/layout';
import { StatCard, PieChart, BarChart } from '@/components/charts';
import { ProductGrid, ProductSearch, ProductFilter } from '@/components/features/products';
import { Card, CardHeader, CardContent, Spinner, Modal } from '@/components/ui';
import { useProducts, useCategories, useSeries } from '@/hooks';
import {
  filterProducts,
  countByCategory,
  countBySeries,
  countElectric,
} from '@/utils';
import type { Product } from '@/api';

export function DashboardPage() {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedSeries, setSelectedSeries] = useState('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  const { data: products, isLoading: productsLoading } = useProducts();
  const { data: categories } = useCategories();
  const { data: series } = useSeries();

  const filteredProducts = useMemo(() => {
    if (!products) return [];
    return filterProducts(products, {
      category: selectedCategory || undefined,
      series: selectedSeries || undefined,
      search: search || undefined,
    });
  }, [products, selectedCategory, selectedSeries, search]);

  const categoryData = useMemo(() => {
    if (!products) return [];
    return countByCategory(products);
  }, [products]);

  const seriesData = useMemo(() => {
    if (!products) return [];
    return countBySeries(products);
  }, [products]);

  const electricCount = useMemo(() => {
    if (!products) return 0;
    return countElectric(products);
  }, [products]);

  const handleCategoryClick = (name: string) => {
    setSelectedCategory(name === selectedCategory ? '' : name);
  };

  const handleSeriesClick = (name: string) => {
    setSelectedSeries(name === selectedSeries ? '' : name);
  };

  if (productsLoading) {
    return (
      <PageContainer className="flex items-center justify-center min-h-[50vh]">
        <Spinner size="lg" />
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          title="Produkte gesamt"
          value={products?.length ?? 0}
          icon={Package}
        />
        <StatCard
          title="Kategorien"
          value={categories?.length ?? 0}
          icon={FolderTree}
        />
        <StatCard
          title="Baureihen"
          value={series?.length ?? 0}
          icon={Layers}
        />
        <StatCard
          title="Elektrofahrzeuge"
          value={electricCount}
          icon={Zap}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-8">
        <PieChart
          title="Verteilung nach Kategorie"
          data={categoryData}
          onSegmentClick={handleCategoryClick}
        />
        <BarChart
          title="Produkte pro Baureihe"
          data={seriesData}
          onBarClick={handleSeriesClick}
        />
      </div>

      {/* Product List */}
      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <h2 className="text-lg font-semibold text-neutral-800">Produkte</h2>
            <div className="flex flex-col sm:flex-row gap-4">
              <ProductSearch value={search} onChange={setSearch} />
              <ProductFilter
                categories={categories ?? []}
                series={series ?? []}
                selectedCategory={selectedCategory}
                selectedSeries={selectedSeries}
                onCategoryChange={setSelectedCategory}
                onSeriesChange={setSelectedSeries}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <ProductGrid
            products={filteredProducts}
            onProductClick={setSelectedProduct}
          />
        </CardContent>
      </Card>

      {/* Product Detail Modal */}
      <Modal
        isOpen={!!selectedProduct}
        onClose={() => setSelectedProduct(null)}
        title={selectedProduct?.name}
        className="max-w-2xl"
      >
        {selectedProduct && (
          <div className="space-y-4">
            {selectedProduct.imageUrl && (
              <img
                src={selectedProduct.imageUrl}
                alt={selectedProduct.name}
                className="w-full h-48 object-cover rounded-lg"
              />
            )}
            <p className="text-neutral-600">{selectedProduct.description}</p>
            
            {selectedProduct.highlights.length > 0 && (
              <div>
                <h4 className="font-medium text-neutral-800 mb-2">Highlights</h4>
                <ul className="list-disc list-inside text-sm text-neutral-600">
                  {selectedProduct.highlights.map((h, i) => (
                    <li key={i}>{h}</li>
                  ))}
                </ul>
              </div>
            )}

            {Object.keys(selectedProduct.specs).length > 0 && (
              <div>
                <h4 className="font-medium text-neutral-800 mb-2">Technische Daten</h4>
                <dl className="grid grid-cols-2 gap-2 text-sm">
                  {Object.entries(selectedProduct.specs).map(([key, value]) => (
                    <div key={key}>
                      <dt className="text-neutral-500">{key}</dt>
                      <dd className="text-neutral-800">{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            )}
          </div>
        )}
      </Modal>
    </PageContainer>
  );
}
