import { api } from './client';
import type { Order, PageResponse } from './types';

export type PaymentMethod = 'STRIPE' | 'VNPAY' | 'MOMO' | 'COD';

export interface CheckoutInput {
  addressId: string;
  paymentMethod: PaymentMethod;
  note?: string;
}

export interface CheckoutResponse {
  order: Order;
  payment: {
    id: string;
    method: PaymentMethod;
    status: string;
    redirectUrl?: string;
    clientSecret?: string;
    expiresAt?: string;
  };
}

export const ordersApi = {
  list: (page = 0, size = 20) =>
    api.get<PageResponse<Order>>('/orders', { params: { page, size } }),
  get: (id: string) => api.get<Order>(`/orders/${id}`),
  preview: (addressId: string) =>
    api.post('/checkout/preview', { addressId }),
  checkout: (data: CheckoutInput) =>
    api.post<CheckoutResponse>('/checkout', data),
};

export const sellerOrdersApi = {
  list: (status?: string, page = 0, size = 20) =>
    api.get('/seller/order-groups', { params: { status, page, size } }),
  update: (groupId: string, data: { status: string; shipment?: { carrier: string; trackingNumber: string } }) =>
    api.patch(`/seller/order-groups/${groupId}`, data),
};
