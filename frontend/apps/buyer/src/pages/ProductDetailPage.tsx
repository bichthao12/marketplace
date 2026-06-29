import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { productsApi } from '@marketplace/api-client';
import { Button } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { useCartStore } from '../stores/cartStore';
import { formatPrice } from '../utils/format';

export function ProductDetailPage() {
  const { slug } = useParams<{ slug: string }>();
  const { t } = useTranslation();
  const addItem = useCartStore((s) => s.addItem);
  const [qty, setQty] = useState(1);

  const product = useQuery({
    queryKey: ['product', slug],
    queryFn: () => productsApi.getBySlug(slug!).then((r) => r.data),
    enabled: !!slug,
  });

  const p = product.data;
  const variant = p?.variants[0];

  const handleAdd = () => {
    if (!p || !variant) return;
    addItem({
      productId: p.id,
      productSlug: p.slug,
      productName: p.name,
      variantId: variant.id,
      variantSku: variant.sku,
      price: variant.price,
      currency: p.currency,
      thumbnailUrl: p.images[0]?.url,
      quantity: qty,
    });
  };

  return (
    <AppShell>
      {product.isLoading && <p>{t('loading')}</p>}
      {product.isError && <p className="text-red-600">{t('error')}</p>}
      {p && variant && (
        <div className="grid gap-8 md:grid-cols-2">
          <div className="aspect-square overflow-hidden rounded-lg bg-gray-100">
            {p.images[0]?.url ? (
              <img src={p.images[0].url} alt={p.name} className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full items-center justify-center text-gray-400">No image</div>
            )}
          </div>
          <div>
            <h1 className="text-2xl font-bold">{p.name}</h1>
            <p className="mt-1 text-sm text-gray-500">{t('seller')}: {p.seller.shopName}</p>
            <p className="mt-4 text-2xl font-semibold text-blue-600">{formatPrice(variant.price, p.currency)}</p>
            <p className="mt-2 text-sm text-gray-600">
              {variant.availableQty > 0 ? t('inStock') : t('outOfStock')} ({variant.availableQty})
            </p>
            {p.description && <p className="mt-4 text-gray-700 whitespace-pre-wrap">{p.description}</p>}
            <div className="mt-6 flex items-center gap-4">
              <label className="text-sm">
                {t('quantity')}
                <input
                  type="number"
                  min={1}
                  max={variant.availableQty}
                  value={qty}
                  onChange={(e) => setQty(Number(e.target.value))}
                  className="ml-2 w-20 rounded border px-2 py-1"
                />
              </label>
              <Button onClick={handleAdd} disabled={variant.availableQty <= 0}>
                {t('addToCart')}
              </Button>
            </div>
          </div>
        </div>
      )}
    </AppShell>
  );
}
