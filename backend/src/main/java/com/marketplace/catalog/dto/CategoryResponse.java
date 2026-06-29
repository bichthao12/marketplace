package com.marketplace.catalog.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        List<CategoryResponse> children
) {
}
