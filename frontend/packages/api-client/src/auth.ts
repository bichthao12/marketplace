import { api } from './client';
import type { AuthResponse } from './types';

export interface RegisterInput {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
}

export interface LoginInput {
  email: string;
  password: string;
}

export const authApi = {
  register: (data: RegisterInput) => api.post<AuthResponse>('/auth/register', data),
  login: (data: LoginInput) => api.post<AuthResponse>('/auth/login', data),
  logout: (refreshToken: string) => api.post('/auth/logout', { refreshToken }),
  refresh: (refreshToken: string) => api.post<AuthResponse>('/auth/refresh', { refreshToken }),
};
