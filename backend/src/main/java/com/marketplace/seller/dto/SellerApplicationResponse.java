package com.marketplace.seller.dto;

import java.time.Instant;
import java.util.UUID;

public record SellerApplicationResponse(
        UUID id,
        String shopName,
        String slug,
        String status,
        Instant submittedAt
) {
}
