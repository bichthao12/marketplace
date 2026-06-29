package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SellerOrderGroupSummary(
        UUID id,
        String orderNumber,
        String status,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        int itemCount,
        Map<String, String> buyer,
        Instant createdAt
) {
}
