import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import type { ProductSummary } from '@marketplace/api-client';
import { Card } from '@marketplace/ui';
import { formatPrice } from '../utils/format';

interface ProductCardProps {
  product: ProductSummary;
}

export function ProductCard({ product }: ProductCardProps) {
  const { t } = useTranslation();
  return (
    <Link to={`/products/${product.slug}`}>
      <Card className="h-full transition hover:shadow-md">
        <div className="mb-3 aspect-square overflow-hidden rounded-md bg-gray-100">
          {product.thumbnailUrl ? (
            <img src={product.thumbnailUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full items-center justify-center text-gray-400 text-sm">No image</div>
          )}
        </div>
        <h3 className="font-medium text-gray-900 line-clamp-2">{product.name}</h3>
        <p className="mt-1 text-sm text-gray-500">{product.seller.shopName}</p>
        <p className="mt-2 font-semibold text-blue-600">{formatPrice(product.priceFrom, product.currency)}</p>
        <p className="text-xs text-gray-500">{product.inStock ? t('inStock') : t('outOfStock')}</p>
      </Card>
    </Link>
  );
}
