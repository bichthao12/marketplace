package com.marketplace.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 10000) String description,
        UUID categoryId
) {
}
