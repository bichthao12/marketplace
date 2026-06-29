import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { categoriesApi, sellerApi } from '@marketplace/api-client';
import { Button, Card, Input } from '@marketplace/ui';
import { SellerShell } from '../components/SellerShell';

export function CreateProductPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    description: '',
    categoryId: '',
    sku: '',
    price: '',
    quantity: '1',
  });

  const categories = useQuery({
    queryKey: ['categories-flat'],
    queryFn: async () => {
      const { data } = await categoriesApi.list(undefined, 3);
      const flat: { id: string; name: string }[] = [];
      const walk = (items: typeof data.data) => {
        items.forEach((c) => {
          flat.push({ id: c.id, name: c.name });
          if (c.children?.length) walk(c.children);
        });
      };
      walk(data.data);
      return flat;
    },
  });

  const createMutation = useMutation({
    mutationFn: () =>
      sellerApi.createProduct({
        name: form.name,
        description: form.description || undefined,
        categoryId: form.categoryId,
        variants: [{
          sku: form.sku,
          price: Number(form.price),
          attributes: { default: 'true' },
          quantity: Number(form.quantity),
        }],
      }),
    onSuccess: () => navigate('/products'),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate();
  };

  return (
    <SellerShell>
      <Card className="max-w-lg">
        <h2 className="mb-4 text-xl font-semibold">{t('createProduct')}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label={t('productName')} required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <div>
            <label className="block text-sm font-medium text-gray-700">{t('description')}</label>
            <textarea
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              rows={3}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">{t('category')}</label>
            <select
              required
              className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
              value={form.categoryId}
              onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            >
              <option value="">—</option>
              {categories.data?.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <Input label={t('sku')} required value={form.sku} onChange={(e) => setForm({ ...form, sku: e.target.value })} />
          <Input label={t('price')} type="number" required min="0.01" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} />
          <Input label={t('quantity')} type="number" required min="0" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
          {createMutation.isError && <p className="text-sm text-red-600">{t('error')}</p>}
          <Button type="submit" loading={createMutation.isPending}>{t('save')}</Button>
        </form>
      </Card>
    </SellerShell>
  );
}
