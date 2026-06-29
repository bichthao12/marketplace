import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button, Card } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { useCartStore } from '../stores/cartStore';
import { formatPrice } from '../utils/format';

export function CartPage() {
  const { t } = useTranslation();
  const { items, updateQuantity, removeItem, total } = useCartStore();

  if (items.length === 0) {
    return (
      <AppShell>
        <p className="text-gray-600">{t('emptyCart')}</p>
        <Link to="/products" className="mt-4 inline-block">
          <Button variant="secondary">{t('continueShopping')}</Button>
        </Link>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <div className="space-y-4">
        {items.map((item) => (
          <Card key={item.variantId} className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex gap-4">
              {item.thumbnailUrl && (
                <img src={item.thumbnailUrl} alt="" className="h-16 w-16 rounded object-cover" />
              )}
              <div>
                <p className="font-medium">{item.productName}</p>
                <p className="text-sm text-gray-500">{item.variantSku}</p>
                <p className="text-sm font-semibold">{formatPrice(item.price, item.currency)}</p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={1}
                value={item.quantity}
                onChange={(e) => updateQuantity(item.variantId, Number(e.target.value))}
                className="w-16 rounded border px-2 py-1 text-sm"
              />
              <Button variant="ghost" onClick={() => removeItem(item.variantId)}>{t('remove')}</Button>
            </div>
          </Card>
        ))}
      </div>
      <div className="mt-6 flex items-center justify-between border-t pt-4">
        <p className="text-lg font-semibold">
          {t('total')}: {formatPrice(total(), items[0]?.currency)}
        </p>
        <Link to="/checkout"><Button>{t('checkout')}</Button></Link>
      </div>
    </AppShell>
  );
}
