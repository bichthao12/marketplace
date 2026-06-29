import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button, Card, Input } from '@marketplace/ui';
import { AppShell } from '../components/AppShell';
import { useAuthStore } from '../stores/authStore';

export function LoginPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((s) => s.login);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const from = (location.state as { from?: string })?.from ?? '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(email, password);
      navigate(from);
    } catch {
      setError(t('error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AppShell>
      <Card className="mx-auto max-w-md">
        <h2 className="mb-4 text-xl font-semibold">{t('login')}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label={t('email')} type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          <Input label={t('password')} type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          {error && <p className="text-sm text-red-600">{error}</p>}
          <Button type="submit" loading={loading} className="w-full">{t('login')}</Button>
        </form>
        <p className="mt-4 text-center text-sm text-gray-600">
          <Link to="/register" className="text-blue-600 hover:underline">{t('register')}</Link>
        </p>
      </Card>
    </AppShell>
  );
}
