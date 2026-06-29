import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { categoriesApi, productsApi } from '@marketplace/api-client';
import { AppShell } from '../components/AppShell';
import { ProductCard } from '../components/ProductCard';

export function HomePage() {
  const { t } = useTranslation();
  const products = useQuery({
    queryKey: ['products', 'featured'],
    queryFn: () => productsApi.search({ page: 0, size: 8 }).then((r) => r.data),
  });
  const categories = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoriesApi.list(undefined, 1).then((r) => r.data.data),
  });

  return (
    <AppShell>
      <section className="mb-8">
        <h2 className="text-2xl font-bold text-gray-900">{t('welcome')}</h2>
        <p className="mt-2 text-gray-600">MVP ecommerce marketplace</p>
      </section>

      {categories.data && categories.data.length > 0 && (
        <section className="mb-8">
          <h3 className="mb-4 text-lg font-semibold">{t('categories')}</h3>
          <div className="flex flex-wrap gap-2">
            {categories.data.map((cat) => (
              <span key={cat.id} className="rounded-full bg-white px-3 py-1 text-sm border border-gray-200">
                {cat.name}
              </span>
            ))}
          </div>
        </section>
      )}

      <section>
        <h3 className="mb-4 text-lg font-semibold">{t('featured')}</h3>
        {products.isLoading && <p>{t('loading')}</p>}
        {products.isError && <p className="text-red-600">{t('error')}</p>}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {products.data?.data.map((p) => <ProductCard key={p.id} product={p} />)}
        </div>
      </section>
    </AppShell>
  );
}
