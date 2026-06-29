package com.marketplace.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminSellerResponse(
        UUID id,
        UUID userId,
        String shopName,
        String slug,
        String description,
        String status,
        String userEmail,
        String userFullName,
        Instant createdAt
) {
}
