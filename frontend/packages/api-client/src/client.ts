import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { AuthResponse } from './types';

const BASE_URL = 'http://localhost:8080/api/v1';

let accessToken: string | null = null;
let refreshToken: string | null = null;
let storageKey = 'marketplace_auth';

type AuthListener = (tokens: { accessToken: string | null; refreshToken: string | null }) => void;
const listeners = new Set<AuthListener>();

export function configureAuthStorage(key: string) {
  storageKey = key;
}

export function getAccessToken() {
  return accessToken;
}

export function getRefreshToken() {
  return refreshToken;
}

export function setAuthTokens(access: string, refresh: string) {
  accessToken = access;
  refreshToken = refresh;
  persistTokens();
  notifyListeners();
}

export function clearAuthTokens() {
  accessToken = null;
  refreshToken = null;
  if (typeof localStorage !== 'undefined') {
    localStorage.removeItem(storageKey);
  }
  notifyListeners();
}

export function loadAuthFromStorage() {
  if (typeof localStorage === 'undefined') return;
  const raw = localStorage.getItem(storageKey);
  if (!raw) return;
  try {
    const parsed = JSON.parse(raw) as { accessToken: string; refreshToken: string };
    accessToken = parsed.accessToken;
    refreshToken = parsed.refreshToken;
  } catch {
    localStorage.removeItem(storageKey);
  }
}

export function subscribeAuth(listener: AuthListener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function persistTokens() {
  if (typeof localStorage === 'undefined' || !accessToken || !refreshToken) return;
  localStorage.setItem(storageKey, JSON.stringify({ accessToken, refreshToken }));
}

function notifyListeners() {
  listeners.forEach((l) => l({ accessToken, refreshToken }));
}

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

let refreshPromise: Promise<string | null> | null = null;

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config;
    if (!original || error.response?.status !== 401 || original.url?.includes('/auth/refresh')) {
      return Promise.reject(error);
    }

    if (!refreshToken) {
      clearAuthTokens();
      return Promise.reject(error);
    }

    if (!refreshPromise) {
      refreshPromise = api
        .post<AuthResponse>('/auth/refresh', { refreshToken })
        .then((res) => {
          setAuthTokens(res.data.accessToken, res.data.refreshToken);
          return res.data.accessToken;
        })
        .catch(() => {
          clearAuthTokens();
          return null;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }

    const newToken = await refreshPromise;
    if (!newToken) return Promise.reject(error);

    original.headers.Authorization = `Bearer ${newToken}`;
    return api(original);
  }
);

export { BASE_URL };
