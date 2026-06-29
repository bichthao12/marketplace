package com.marketplace.cart.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartData {

    private UUID id;
    private String currency;
    private List<CartItem> items = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
