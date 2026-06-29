import { api } from './client';
import type { PageResponse, ProductDetail, ProductStatus } from './types';

export interface CreateVariantInput {
  sku: string;
  price: number;
  compareAtPrice?: number;
  attributes: Record<string, string>;
  quantity: number;
}

export interface CreateProductInput {
  name: string;
  description?: string;
  categoryId: string;
  variants: CreateVariantInput[];
}

export interface UpdateProductStatusInput {
  status: ProductStatus;
}

export const sellerApi = {
  listProducts: (status?: ProductStatus, page = 0, size = 20) =>
    api.get<PageResponse<ProductDetail>>('/seller/products', { params: { status, page, size } }),
  getProduct: (id: string) => api.get<ProductDetail>(`/seller/products/${id}`),
  createProduct: (data: CreateProductInput) => api.post<ProductDetail>('/seller/products', data),
  updateStatus: (id: string, data: UpdateProductStatusInput) =>
    api.patch<ProductDetail>(`/seller/products/${id}/status`, data),
};
