-- User shipping addresses
CREATE TABLE user_addresses (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label           VARCHAR(50),
    recipient_name  VARCHAR(100) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    line1           VARCHAR(255) NOT NULL,
    line2           VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(2) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

-- Orders
CREATE TABLE orders (
    id               UUID PRIMARY KEY,
    order_number     VARCHAR(30) NOT NULL UNIQUE,
    buyer_id         UUID NOT NULL REFERENCES users(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'pending_payment',
    payment_status   VARCHAR(30) NOT NULL DEFAULT 'pending',
    currency         VARCHAR(3) NOT NULL,
    subtotal         NUMERIC(14, 2) NOT NULL,
    total_shipping   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    grand_total      NUMERIC(14, 2) NOT NULL,
    shipping_address JSONB NOT NULL,
    note             TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_groups (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    seller_id     UUID NOT NULL REFERENCES seller_profiles(id),
    status        VARCHAR(30) NOT NULL DEFAULT 'new',
    subtotal      NUMERIC(14, 2) NOT NULL,
    shipping_fee  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_groups_order_id ON order_groups(order_id);
CREATE INDEX idx_order_groups_seller_id ON order_groups(seller_id);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY,
    order_group_id  UUID NOT NULL REFERENCES order_groups(id) ON DELETE CASCADE,
    variant_id      UUID NOT NULL REFERENCES product_variants(id),
    product_name    VARCHAR(200) NOT NULL,
    sku             VARCHAR(64) NOT NULL,
    variant_attrs   JSONB NOT NULL DEFAULT '{}',
    unit_price      NUMERIC(12, 2) NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    line_total      NUMERIC(14, 2) NOT NULL
);

CREATE INDEX idx_order_items_group_id ON order_items(order_group_id);

CREATE TABLE payments (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    method        VARCHAR(20) NOT NULL,
    status        VARCHAR(30) NOT NULL DEFAULT 'pending',
    amount        NUMERIC(14, 2) NOT NULL,
    currency      VARCHAR(3) NOT NULL,
    external_id   VARCHAR(255),
    client_secret VARCHAR(500),
    redirect_url  VARCHAR(1000),
    metadata      JSONB DEFAULT '{}',
    paid_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_external_id ON payments(external_id);

CREATE TABLE shipments (
    id              UUID PRIMARY KEY,
    order_group_id  UUID NOT NULL REFERENCES order_groups(id) ON DELETE CASCADE,
    carrier         VARCHAR(30) NOT NULL,
    tracking_number VARCHAR(100),
    status          VARCHAR(30) NOT NULL DEFAULT 'pending',
    shipped_at      TIMESTAMPTZ,
    delivered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY,
    actor_id    UUID REFERENCES users(id),
    action      VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id   UUID NOT NULL,
    metadata    JSONB DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
