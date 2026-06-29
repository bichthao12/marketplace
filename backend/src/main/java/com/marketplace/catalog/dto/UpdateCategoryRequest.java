package com.marketplace.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateCategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 120) String slug,
        UUID parentId,
        int sortOrder,
        String imageUrl
) {
}
