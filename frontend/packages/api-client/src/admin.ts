import { api } from './client';
import type { Category, Order, PageResponse, SellerProfile } from './types';

export interface CreateCategoryInput {
  name: string;
  slug: string;
  parentId?: string;
  sortOrder?: number;
  imageUrl?: string;
}

export interface UpdateCategoryInput {
  name?: string;
  slug?: string;
  parentId?: string;
  sortOrder?: number;
  imageUrl?: string;
}

export const adminApi = {
  listCategories: () => api.get<{ data: Category[] }>('/admin/categories'),
  getCategory: (id: string) => api.get<Category>(`/admin/categories/${id}`),
  createCategory: (data: CreateCategoryInput) => api.post<Category>('/admin/categories', data),
  updateCategory: (id: string, data: UpdateCategoryInput) =>
    api.put<Category>(`/admin/categories/${id}`, data),
  deleteCategory: (id: string) => api.delete(`/admin/categories/${id}`),

  listPendingSellers: (page = 0) =>
    api.get('/admin/sellers', { params: { status: 'PENDING', page, size: 20 } }),
  approveSeller: (id: string) =>
    api.patch(`/admin/sellers/${id}`, { action: 'approve' }),
  rejectSeller: (id: string, reason = 'Rejected') =>
    api.patch(`/admin/sellers/${id}`, { action: 'reject', reason }),

  listOrders: (page = 0, size = 20) =>
    api.get<PageResponse<Order>>('/admin/orders', { params: { page, size } }),
};
