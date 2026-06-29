import { api } from './client';

export interface CartItemDto {
  id: string;
  variantId: string;
  productName: string;
  variantAttributes: Record<string, string>;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  availableQty: number;
}

export interface CartGroupDto {
  seller: { id: string; shopName: string; slug: string };
  items: CartItemDto[];
  subtotal: number;
}

export interface CartResponse {
  id: string;
  currency: string;
  itemCount: number;
  subtotal: number;
  groups: CartGroupDto[];
  warnings: string[];
}

export const cartApi = {
  get: () => api.get<CartResponse>('/cart'),
  addItem: (variantId: string, quantity: number) =>
    api.post<CartResponse>('/cart/items', { variantId, quantity }),
  updateItem: (itemId: string, quantity: number) =>
    api.put<CartResponse>(`/cart/items/${itemId}`, { quantity }),
  removeItem: (itemId: string) => api.delete<CartResponse>(`/cart/items/${itemId}`),
  merge: (guestSessionId: string) => api.post<CartResponse>('/cart/merge', { guestSessionId }),
  clear: () => api.delete('/cart'),
};
