package com.marketplace.catalog.dto;

import java.util.UUID;

public record SellerSummaryResponse(
        UUID id,
        String shopName,
        String slug
) {
}
