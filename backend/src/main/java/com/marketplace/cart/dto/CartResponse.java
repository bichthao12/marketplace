package com.marketplace.cart.dto;

import com.marketplace.catalog.dto.SellerSummaryResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CartResponse(
        UUID id,
        String currency,
        int itemCount,
        BigDecimal subtotal,
        List<CartGroupResponse> groups,
        List<String> warnings
) {
    public record CartGroupResponse(
            SellerSummaryResponse seller,
            List<CartItemResponse> items,
            BigDecimal subtotal
    ) {
    }

    public record CartItemResponse(
            UUID id,
            UUID variantId,
            String productName,
            Map<String, String> variantAttributes,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            int availableQty
    ) {
    }
}
