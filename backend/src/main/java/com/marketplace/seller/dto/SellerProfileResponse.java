package com.marketplace.seller.dto;

import java.util.Map;
import java.util.UUID;

public record SellerProfileResponse(
        UUID id,
        String shopName,
        String slug,
        String description,
        String logoUrl,
        String status,
        String currency,
        Map<String, Object> stats
) {
}
