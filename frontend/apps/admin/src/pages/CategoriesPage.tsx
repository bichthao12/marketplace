import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '@marketplace/api-client';
import { Button, Card, Input } from '@marketplace/ui';
import { AdminShell } from '../components/AdminShell';

export function CategoriesPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState({ name: '', slug: '' });

  const categories = useQuery({
    queryKey: ['admin-categories'],
    queryFn: () => adminApi.listCategories().then((r) => r.data.data),
  });

  const saveMutation = useMutation({
    mutationFn: () =>
      editingId
        ? adminApi.updateCategory(editingId, { name: form.name, slug: form.slug })
        : adminApi.createCategory({ name: form.name, slug: form.slug, sortOrder: 0 }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-categories'] });
      setShowForm(false);
      setEditingId(null);
      setForm({ name: '', slug: '' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.deleteCategory(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-categories'] }),
  });

  const startEdit = (id: string, name: string, slug: string) => {
    setEditingId(id);
    setForm({ name, slug });
    setShowForm(true);
  };

  const flatten = (items: typeof categories.data, depth = 0): { id: string; name: string; slug: string; depth: number }[] => {
    if (!items) return [];
    return items.flatMap((c) => [
      { id: c.id, name: c.name, slug: c.slug, depth },
      ...flatten(c.children, depth + 1),
    ]);
  };

  return (
    <AdminShell>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-semibold">{t('categories')}</h2>
        <Button onClick={() => { setShowForm(true); setEditingId(null); setForm({ name: '', slug: '' }); }}>
          {t('create')}
        </Button>
      </div>

      {showForm && (
        <Card className="mb-4 max-w-md">
          <form
            onSubmit={(e) => { e.preventDefault(); saveMutation.mutate(); }}
            className="space-y-3"
          >
            <Input label={t('name')} required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <Input label={t('slug')} required value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            <div className="flex gap-2">
              <Button type="submit" loading={saveMutation.isPending}>{t('save')}</Button>
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>{t('cancel')}</Button>
            </div>
          </form>
        </Card>
      )}

      {categories.isLoading && <p>{t('loading')}</p>}
      <div className="space-y-2">
        {flatten(categories.data).map((cat) => (
          <Card key={cat.id} className="flex items-center justify-between" style={{ marginLeft: cat.depth * 16 }}>
            <div>
              <p className="font-medium">{cat.name}</p>
              <p className="text-sm text-gray-500">{cat.slug}</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={() => startEdit(cat.id, cat.name, cat.slug)}>{t('edit')}</Button>
              <Button variant="danger" onClick={() => deleteMutation.mutate(cat.id)}>{t('delete')}</Button>
            </div>
          </Card>
        ))}
      </div>
    </AdminShell>
  );
}
