-- Seller profiles (dependency for catalog)
CREATE TABLE seller_profiles (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    shop_name   VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    logo_url    VARCHAR(500),
    status      VARCHAR(20) NOT NULL DEFAULT 'pending',
    currency    VARCHAR(3) NOT NULL DEFAULT 'VND',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seller_profiles_slug ON seller_profiles(slug);
CREATE INDEX idx_seller_profiles_status ON seller_profiles(status);

-- Categories (admin-managed tree)
CREATE TABLE categories (
    id          UUID PRIMARY KEY,
    parent_id   UUID REFERENCES categories(id) ON DELETE SET NULL,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    image_url   VARCHAR(500),
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);

-- Products
CREATE TABLE products (
    id           UUID PRIMARY KEY,
    seller_id    UUID NOT NULL REFERENCES seller_profiles(id) ON DELETE CASCADE,
    category_id  UUID NOT NULL REFERENCES categories(id),
    name         VARCHAR(200) NOT NULL,
    slug         VARCHAR(220) NOT NULL UNIQUE,
    description  TEXT,
    currency     VARCHAR(3) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'draft',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_slug ON products(slug);

-- Full-text search (PostgreSQL)
ALTER TABLE products ADD COLUMN search_vector tsvector;
CREATE INDEX idx_products_search ON products USING GIN(search_vector);

CREATE OR REPLACE FUNCTION products_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('simple', coalesce(NEW.name, '') || ' ' || coalesce(NEW.description, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_search_vector_trigger
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION products_search_vector_update();

-- Variants
CREATE TABLE product_variants (
    id               UUID PRIMARY KEY,
    product_id       UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku              VARCHAR(64) NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,
    compare_at_price NUMERIC(12, 2),
    attributes       JSONB NOT NULL DEFAULT '{}',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, sku)
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);

-- Inventory (1:1 with variant)
CREATE TABLE inventory (
    variant_id    UUID PRIMARY KEY REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity      INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved_qty  INT NOT NULL DEFAULT 0 CHECK (reserved_qty >= 0)
);

-- Product images
CREATE TABLE product_images (
    id          UUID PRIMARY KEY,
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
