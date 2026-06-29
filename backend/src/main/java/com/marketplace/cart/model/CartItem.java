package com.marketplace.cart.model;

import java.time.Instant;
import java.util.UUID;

public class CartItem {

    private UUID id;
    private UUID variantId;
    private int quantity;
    private Instant reservedUntil;

    public CartItem() {
    }

    public CartItem(UUID id, UUID variantId, int quantity, Instant reservedUntil) {
        this.id = id;
        this.variantId = variantId;
        this.quantity = quantity;
        this.reservedUntil = reservedUntil;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public void setVariantId(UUID variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(Instant reservedUntil) {
        this.reservedUntil = reservedUntil;
    }
}
