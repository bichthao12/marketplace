import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ordersApi } from '@marketplace/api-client';
import { Card } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { useAuthStore } from '../stores/authStore';
import { formatPrice } from '../utils/format';

export function OrdersPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();

  const orders = useQuery({
    queryKey: ['orders'],
    queryFn: () => ordersApi.list().then((r) => r.data),
    enabled: isAuthenticated,
    retry: false,
  });

  useEffect(() => {
    if (!isAuthenticated) navigate('/login', { state: { from: '/orders' } });
  }, [isAuthenticated, navigate]);

  return (
    <AppShell>
      <h2 className="mb-4 text-xl font-semibold">{t('orders')}</h2>
      {orders.isLoading && <p>{t('loading')}</p>}
      {orders.isError && <p className="text-gray-600">{t('noOrders')}</p>}
      <div className="space-y-4">
        {orders.data?.data.map((order) => (
          <Card key={order.id}>
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <p className="font-medium">{t('orderNumber')}{order.orderNumber}</p>
                <p className="text-sm text-gray-500">{new Date(order.createdAt).toLocaleString()}</p>
              </div>
              <div className="text-right">
                <p className="text-sm">{t('status')}: {order.status}</p>
                <p className="font-semibold">{formatPrice(order.totalAmount, order.currency)}</p>
              </div>
            </div>
            <ul className="mt-3 border-t pt-3 text-sm text-gray-600">
              {order.items?.map((item) => (
                <li key={item.id}>{item.productName} x{item.quantity}</li>
              ))}
            </ul>
          </Card>
        ))}
      </div>
      {orders.data?.data.length === 0 && <p className="text-gray-500">{t('noOrders')}</p>}
    </AppShell>
  );
}
