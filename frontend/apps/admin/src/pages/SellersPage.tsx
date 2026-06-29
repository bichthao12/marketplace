import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '@marketplace/api-client';
import { Button, Card } from '@marketplace/ui';
import { AdminShell } from '../components/AdminShell';

export function SellersPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const sellers = useQuery({
    queryKey: ['pending-sellers'],
    queryFn: () => adminApi.listPendingSellers().then((r) => r.data.data ?? []),
    retry: false,
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => adminApi.approveSeller(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pending-sellers'] }),
  });

  const rejectMutation = useMutation({
    mutationFn: (id: string) => adminApi.rejectSeller(id, 'Rejected by admin'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pending-sellers'] }),
  });

  return (
    <AdminShell>
      <h2 className="mb-4 text-xl font-semibold">{t('sellers')}</h2>
      {sellers.isLoading && <p>{t('loading')}</p>}
      {sellers.isError && <p className="text-gray-600">{t('noSellers')}</p>}
      <div className="space-y-3">
        {sellers.data?.map((seller) => (
          <Card key={seller.id} className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="font-medium">{seller.shopName}</p>
              <p className="text-sm text-gray-500">{seller.slug} · {t('status')}: {seller.status}</p>
            </div>
            <div className="flex gap-2">
              <Button onClick={() => approveMutation.mutate(seller.id)} loading={approveMutation.isPending}>
                {t('approve')}
              </Button>
              <Button variant="danger" onClick={() => rejectMutation.mutate(seller.id)} loading={rejectMutation.isPending}>
                {t('reject')}
              </Button>
            </div>
          </Card>
        ))}
      </div>
      {sellers.data?.length === 0 && <p className="text-gray-500">{t('noSellers')}</p>}
    </AdminShell>
  );
}
