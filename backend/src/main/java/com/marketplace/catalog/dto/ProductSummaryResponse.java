package com.marketplace.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String name,
        String slug,
        String thumbnailUrl,
        BigDecimal priceFrom,
        String currency,
        SellerSummaryResponse seller,
        boolean inStock
) {
}
