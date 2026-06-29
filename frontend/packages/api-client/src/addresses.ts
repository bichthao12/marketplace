import { api } from './client';

export interface AddressDto {
  id: string;
  label?: string;
  recipientName: string;
  phone: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  isDefault: boolean;
}

export interface AddressInput {
  label?: string;
  recipientName: string;
  phone: string;
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  isDefault?: boolean;
}

export const addressesApi = {
  list: () => api.get<{ data: AddressDto[] }>('/users/me/addresses'),
  create: (data: AddressInput) => api.post<AddressDto>('/users/me/addresses', { ...data, isDefault: data.isDefault ?? true }),
};
