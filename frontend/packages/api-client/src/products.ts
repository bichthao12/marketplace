import { api } from './client';
import type { Category, PageResponse, ProductDetail, ProductSummary } from './types';

export interface ProductSearchParams {
  q?: string;
  categorySlug?: string;
  sellerSlug?: string;
  currency?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const productsApi = {
  search: (params: ProductSearchParams = {}) =>
    api.get<PageResponse<ProductSummary>>('/products', { params }),
  getBySlug: (slug: string) => api.get<ProductDetail>(`/products/${slug}`),
};

export const categoriesApi = {
  list: (parentId?: string, depth = 2) =>
    api.get<{ data: Category[] }>('/categories', { params: { parentId, depth } }),
};
