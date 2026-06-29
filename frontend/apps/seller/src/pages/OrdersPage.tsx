import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { sellerOrdersApi } from '@marketplace/api-client';
import { Button, Card } from '@marketplace/ui';
import { SellerShell } from '../components/SellerShell';

export function OrdersPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const orders = useQuery({
    queryKey: ['seller-orders'],
    queryFn: () => sellerOrdersApi.list().then((r) => r.data),
    retry: false,
  });

  const fulfillMutation = useMutation({
    mutationFn: (groupId: string) => sellerOrdersApi.fulfill(groupId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['seller-orders'] }),
  });

  return (
    <SellerShell>
      <h2 className="mb-4 text-xl font-semibold">{t('orders')}</h2>
      {orders.isLoading && <p>{t('loading')}</p>}
      {orders.isError && <p className="text-gray-600">{t('noOrders')}</p>}
      <div className="space-y-3">
        {orders.data?.data.map((order) => (
          <Card key={order.id} className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="font-medium">#{order.orderNumber}</p>
              <p className="text-sm text-gray-500">{t('status')}: {order.status}</p>
            </div>
            {order.status === 'PAID' && (
              <Button onClick={() => fulfillMutation.mutate(order.id)} loading={fulfillMutation.isPending}>
                {t('fulfill')}
              </Button>
            )}
            {order.status === 'PROCESSING' && (
              <Button variant="secondary" onClick={() => sellerOrdersApi.ship(order.id)}>
                {t('ship')}
              </Button>
            )}
          </Card>
        ))}
      </div>
      {orders.data?.data.length === 0 && <p className="text-gray-500">{t('noOrders')}</p>}
    </SellerShell>
  );
}
