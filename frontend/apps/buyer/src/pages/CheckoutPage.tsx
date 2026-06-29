import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { addressesApi, cartApi, ordersApi } from '@marketplace/api-client';
import { Button, Card, Input } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { useAuthStore } from '../stores/authStore';
import { useCartStore } from '../stores/cartStore';
import { formatPrice } from '../utils/format';

export function CheckoutPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthStore();
  const { items, total, clear } = useCartStore();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    recipientName: '',
    phone: '',
    line1: '',
    city: '',
    country: 'VN',
  });

  if (!isAuthenticated) {
    navigate('/login', { state: { from: '/checkout' } });
    return null;
  }

  if (items.length === 0) {
    navigate('/cart');
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      for (const item of items) {
        await cartApi.addItem(item.variantId, item.quantity);
      }
      const addressRes = await addressesApi.create({
        recipientName: form.recipientName,
        phone: form.phone,
        line1: form.line1,
        city: form.city,
        country: form.country,
        isDefault: true,
      });
      await ordersApi.checkout({
        addressId: addressRes.data.id,
        paymentMethod: 'COD',
      });
      await cartApi.clear();
      clear();
      navigate('/orders');
    } catch {
      setError(t('error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <div className="grid gap-8 md:grid-cols-2">
        <Card>
          <h2 className="mb-4 text-lg font-semibold">{t('shippingAddress')}</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input label={t('fullName')} required value={form.recipientName} onChange={(e) => setForm({ ...form, recipientName: e.target.value })} />
            <Input label={t('phone')} required value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            <Input label={t('address')} required value={form.line1} onChange={(e) => setForm({ ...form, line1: e.target.value })} />
            <Input label={t('city')} required value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
            {error && <p className="text-sm text-red-600">{error}</p>}
            <Button type="submit" loading={loading}>{t('placeOrder')}</Button>
          </form>
        </Card>
        <Card>
          <h2 className="mb-4 text-lg font-semibold">{t('cart')}</h2>
          <ul className="space-y-2 text-sm">
            {items.map((i) => (
              <li key={i.variantId} className="flex justify-between">
                <span>{i.productName} x{i.quantity}</span>
                <span>{formatPrice(i.price * i.quantity, i.currency)}</span>
              </li>
            ))}
          </ul>
          <p className="mt-4 border-t pt-4 font-semibold">
            {t('total')}: {formatPrice(total(), items[0]?.currency)}
          </p>
          <p className="mt-2 text-sm text-gray-500">{t('paymentMethod')}: COD</p>
        </Card>
      </div>
    </AppShell>
  );
}
