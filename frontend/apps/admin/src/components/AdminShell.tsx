import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Layout, Button } from '@marketplace/ui';
import { useAuthStore } from '../stores/authStore';

export function AdminShell({ children }: { children: React.ReactNode }) {
  const { t, i18n } = useTranslation();
  const { isAuthenticated, user, logout } = useAuthStore();

  if (!isAuthenticated) return <>{children}</>;

  return (
    <Layout
      title={t('appName')}
      nav={
        <>
          <Link to="/sellers" className="text-gray-600 hover:text-gray-900">{t('sellers')}</Link>
          <Link to="/categories" className="text-gray-600 hover:text-gray-900">{t('categories')}</Link>
          <Link to="/orders" className="text-gray-600 hover:text-gray-900">{t('orders')}</Link>
        </>
      }
      actions={
        <>
          <Button variant="ghost" onClick={() => i18n.changeLanguage(i18n.language === 'vi' ? 'en' : 'vi')}>
            {i18n.language === 'vi' ? 'EN' : 'VI'}
          </Button>
          <span className="text-sm text-gray-600">{user?.fullName}</span>
          <Button variant="secondary" onClick={() => logout()}>{t('logout')}</Button>
        </>
      }
    >
      {children}
    </Layout>
  );
}
