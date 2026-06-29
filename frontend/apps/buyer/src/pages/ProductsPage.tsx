import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { productsApi } from '@marketplace/api-client';
import { Input } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { ProductCard } from '../components/ProductCard';

export function ProductsPage() {
  const { t } = useTranslation();
  const [q, setQ] = useState('');
  const [search, setSearch] = useState('');

  const products = useQuery({
    queryKey: ['products', search],
    queryFn: () => productsApi.search({ q: search || undefined, page: 0, size: 24 }).then((r) => r.data),
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(q);
  };

  return (
    <AppShell>
      <form onSubmit={handleSearch} className="mb-6 flex gap-2">
        <Input
          className="flex-1"
          placeholder={t('search')}
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700">
          {t('search')}
        </button>
      </form>

      {products.isLoading && <p>{t('loading')}</p>}
      {products.isError && <p className="text-red-600">{t('error')}</p>}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {products.data?.data.map((p) => <ProductCard key={p.id} product={p} />)}
      </div>
      {products.data?.data.length === 0 && <p className="text-gray-500">No products found</p>}
    </AppShell>
  );
}
