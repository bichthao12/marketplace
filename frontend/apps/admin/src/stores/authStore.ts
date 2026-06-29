import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import {
  authApi,
  clearAuthTokens,
  configureAuthStorage,
  getRefreshToken,
  loadAuthFromStorage,
  setAuthTokens,
  type User,
} from '@marketplace/api-client';

configureAuthStorage('marketplace_admin_auth');
loadAuthFromStorage();

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      login: async (email, password) => {
        const { data } = await authApi.login({ email, password });
        if (!data.user.roles.includes('ADMIN')) {
          throw new Error('Not an admin account');
        }
        setAuthTokens(data.accessToken, data.refreshToken);
        set({ user: data.user, isAuthenticated: true });
      },
      logout: async () => {
        const refresh = getRefreshToken();
        if (refresh) {
          try { await authApi.logout(refresh); } catch { /* ignore */ }
        }
        clearAuthTokens();
        set({ user: null, isAuthenticated: false });
      },
    }),
    { name: 'marketplace_admin_user', partialize: (s) => ({ user: s.user, isAuthenticated: s.isAuthenticated }) }
  )
);
