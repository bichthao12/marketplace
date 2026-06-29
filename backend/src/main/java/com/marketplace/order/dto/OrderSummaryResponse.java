package com.marketplace.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderNumber,
        String status,
        String paymentStatus,
        BigDecimal grandTotal,
        String currency,
        int itemCount,
        Instant createdAt
) {
}
