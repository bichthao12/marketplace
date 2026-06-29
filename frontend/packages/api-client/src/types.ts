export type Role = 'BUYER' | 'SELLER' | 'ADMIN';

export interface User {
  id: string;
  email: string;
  fullName: string;
  roles: Role[];
}

export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  data: T[];
  meta: PageMeta;
}

export interface SellerSummary {
  id: string;
  shopName: string;
  slug: string;
  logoUrl?: string;
}

export interface ProductSummary {
  id: string;
  name: string;
  slug: string;
  thumbnailUrl?: string;
  priceFrom: number;
  currency: string;
  seller: SellerSummary;
  inStock: boolean;
}

export interface CategoryRef {
  id: string;
  name: string;
  slug: string;
}

export interface ProductImage {
  id: string;
  url: string;
  sortOrder: number;
}

export interface Variant {
  id: string;
  sku: string;
  attributes: Record<string, string>;
  price: number;
  compareAtPrice?: number;
  availableQty: number;
}

export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface ProductDetail {
  id: string;
  name: string;
  slug: string;
  description?: string;
  currency: string;
  status: ProductStatus;
  category: CategoryRef;
  seller: SellerSummary;
  images: ProductImage[];
  variants: Variant[];
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  imageUrl?: string;
  children: Category[];
}

export interface SellerProfile {
  id: string;
  userId: string;
  shopName: string;
  slug: string;
  description?: string;
  logoUrl?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED';
  currency: string;
  createdAt: string;
}

export type OrderStatus =
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  id: string;
  productName: string;
  variantSku: string;
  quantity: number;
  unitPrice: number;
  currency: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  currency: string;
  createdAt: string;
  items: OrderItem[];
}

export interface ApiErrorBody {
  error: {
    code: string;
    message: string;
    details?: string[];
  };
}
