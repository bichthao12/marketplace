package com.marketplace.order.dto;

import java.time.Instant;
import java.util.UUID;

public record CheckoutResponse(
        OrderDetailResponse order,
        PaymentResponse payment
) {
    public record PaymentResponse(
            UUID id,
            String method,
            String status,
            String redirectUrl,
            String clientSecret,
            String publishableKey,
            Instant expiresAt
    ) {
    }
}
