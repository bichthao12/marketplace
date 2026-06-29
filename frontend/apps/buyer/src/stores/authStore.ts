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

configureAuthStorage('marketplace_buyer_auth');
loadAuthFromStorage();

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, fullName: string, phone?: string) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      setUser: (user) => set({ user, isAuthenticated: !!user }),
      login: async (email, password) => {
        const { data } = await authApi.login({ email, password });
        setAuthTokens(data.accessToken, data.refreshToken);
        set({ user: data.user, isAuthenticated: true });
      },
      register: async (email, password, fullName, phone) => {
        const { data } = await authApi.register({ email, password, fullName, phone });
        setAuthTokens(data.accessToken, data.refreshToken);
        set({ user: data.user, isAuthenticated: true });
      },
      logout: async () => {
        const refresh = getRefreshToken();
        if (refresh) {
          try {
            await authApi.logout(refresh);
          } catch {
            /* ignore */
          }
        }
        clearAuthTokens();
        set({ user: null, isAuthenticated: false });
      },
    }),
    {
      name: 'marketplace_buyer_user',
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
);
