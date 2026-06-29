package com.marketplace.catalog.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        int sortOrder
) {
}
