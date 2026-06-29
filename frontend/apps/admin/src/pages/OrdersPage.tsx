import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '@marketplace/api-client';
import { Card } from '@marketplace/ui';
import { AdminShell } from '../components/AdminShell';

function formatPrice(amount: number, currency = 'VND') {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency }).format(amount);
}

export function OrdersPage() {
  const { t } = useTranslation();

  const orders = useQuery({
    queryKey: ['admin-orders'],
    queryFn: () => adminApi.listOrders().then((r) => r.data),
    retry: false,
  });

  return (
    <AdminShell>
      <h2 className="mb-4 text-xl font-semibold">{t('orders')}</h2>
      {orders.isLoading && <p>{t('loading')}</p>}
      {orders.isError && <p className="text-gray-600">{t('noOrders')}</p>}
      <div className="space-y-3">
        {orders.data?.data.map((order) => (
          <Card key={order.id} className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="font-medium">{t('orderNumber')}{order.orderNumber}</p>
              <p className="text-sm text-gray-500">{new Date(order.createdAt).toLocaleString()}</p>
            </div>
            <div className="text-right">
              <p className="text-sm">{t('status')}: {order.status}</p>
              <p className="font-semibold">{formatPrice(order.totalAmount, order.currency)}</p>
            </div>
          </Card>
        ))}
      </div>
      {orders.data?.data.length === 0 && <p className="text-gray-500">{t('noOrders')}</p>}
    </AdminShell>
  );
}
