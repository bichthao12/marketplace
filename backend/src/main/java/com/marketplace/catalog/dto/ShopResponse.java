package com.marketplace.catalog.dto;

import java.time.Instant;
import java.util.UUID;

public record ShopResponse(
        UUID id,
        String shopName,
        String slug,
        String description,
        String logoUrl,
        long productCount,
        Instant joinedAt
) {
}
