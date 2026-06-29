import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { sellerApi } from '@marketplace/api-client';
import { Button, Card } from '@marketplace/ui';
import { SellerShell } from '../components/SellerShell';

export function ProductsPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const products = useQuery({
    queryKey: ['seller-products'],
    queryFn: () => sellerApi.listProducts().then((r) => r.data),
  });

  const publishMutation = useMutation({
    mutationFn: (id: string) => sellerApi.updateStatus(id, { status: 'PUBLISHED' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['seller-products'] }),
  });

  return (
    <SellerShell>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-semibold">{t('products')}</h2>
        <Link to="/products/new"><Button>{t('createProduct')}</Button></Link>
      </div>
      {products.isLoading && <p>{t('loading')}</p>}
      {products.isError && <p className="text-red-600">{t('error')}</p>}
      <div className="space-y-3">
        {products.data?.data.map((p) => (
          <Card key={p.id} className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <p className="font-medium">{p.name}</p>
              <p className="text-sm text-gray-500">{t('status')}: {p.status}</p>
            </div>
            {p.status === 'DRAFT' && (
              <Button onClick={() => publishMutation.mutate(p.id)} loading={publishMutation.isPending}>
                {t('publish')}
              </Button>
            )}
          </Card>
        ))}
      </div>
      {products.data?.data.length === 0 && <p className="text-gray-500">{t('noProducts')}</p>}
    </SellerShell>
  );
}
