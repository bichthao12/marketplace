import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { sellerApi, sellerOrdersApi } from '@marketplace/api-client';
import { Card } from '@marketplace/ui';
import { SellerShell } from '../components/SellerShell';

export function DashboardPage() {
  const { t } = useTranslation();
  const products = useQuery({
    queryKey: ['seller-products-count'],
    queryFn: () => sellerApi.listProducts(undefined, 0, 1).then((r) => r.data),
  });
  const orders = useQuery({
    queryKey: ['seller-orders-count'],
    queryFn: () => sellerOrdersApi.list('PAID', 0, 1).then((r) => r.data),
    retry: false,
  });

  return (
    <SellerShell>
      <h2 className="mb-6 text-xl font-semibold">{t('dashboard')}</h2>
      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <p className="text-sm text-gray-500">{t('totalProducts')}</p>
          <p className="text-3xl font-bold">{products.data?.meta.totalElements ?? '—'}</p>
        </Card>
        <Card>
          <p className="text-sm text-gray-500">{t('pendingOrders')}</p>
          <p className="text-3xl font-bold">{orders.data?.meta.totalElements ?? '—'}</p>
        </Card>
      </div>
    </SellerShell>
  );
}
