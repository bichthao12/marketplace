package com.marketplace.order.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentRetryResponse(
        UUID id,
        String method,
        String status,
        String redirectUrl,
        String clientSecret,
        String publishableKey,
        Instant expiresAt
) {
}
