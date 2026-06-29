import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Layout, Button } from '@marketplace/ui';
import { useAuthStore } from '../stores/authStore';
import { useCartStore } from '../stores/cartStore';

interface AppShellProps {
  children: React.ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const { t, i18n } = useTranslation();
  const { isAuthenticated, user, logout } = useAuthStore();
  const cartCount = useCartStore((s) => s.count());

  const toggleLang = () => i18n.changeLanguage(i18n.language === 'vi' ? 'en' : 'vi');

  return (
    <Layout
      title={t('appName')}
      nav={
        <>
          <Link to="/" className="text-gray-600 hover:text-gray-900">{t('home')}</Link>
          <Link to="/products" className="text-gray-600 hover:text-gray-900">{t('products')}</Link>
          <Link to="/cart" className="text-gray-600 hover:text-gray-900">
            {t('cart')} {cartCount > 0 && `(${cartCount})`}
          </Link>
          {isAuthenticated && (
            <Link to="/orders" className="text-gray-600 hover:text-gray-900">{t('orders')}</Link>
          )}
        </>
      }
      actions={
        <>
          <Button variant="ghost" onClick={toggleLang}>{i18n.language === 'vi' ? 'EN' : 'VI'}</Button>
          {isAuthenticated ? (
            <>
              <span className="text-sm text-gray-600">{user?.fullName}</span>
              <Button variant="secondary" onClick={() => logout()}>{t('logout')}</Button>
            </>
          ) : (
            <>
              <Link to="/login"><Button variant="secondary">{t('login')}</Button></Link>
              <Link to="/register"><Button>{t('register')}</Button></Link>
            </>
          )}
        </>
      }
    >
      {children}
    </Layout>
  );
}
