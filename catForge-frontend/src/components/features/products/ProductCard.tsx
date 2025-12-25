import type { Product } from '@/api';
import { Card, Badge } from '@/components/ui';
import { formatPrice } from '@/utils';

interface ProductCardProps {
  product: Product;
  onClick?: () => void;
}

export function ProductCard({ product, onClick }: ProductCardProps) {
  return (
    <Card hoverable onClick={onClick} className="overflow-hidden">
      <div className="aspect-video bg-neutral-100 relative">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-neutral-400">
            Kein Bild
          </div>
        )}
      </div>
      <div className="p-4">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-neutral-800 line-clamp-1">{product.name}</h3>
          <Badge variant="blue">{product.category}</Badge>
        </div>
        <p className="mt-1 text-sm text-neutral-600 line-clamp-2">
          {product.shortDescription}
        </p>
        <div className="mt-3 flex items-center justify-between">
          <span className="text-xs text-neutral-500">{product.series}</span>
          <span className="font-medium text-neutral-800">
            {formatPrice(product.priceEur)}
          </span>
        </div>
      </div>
    </Card>
  );
}
