package com.marketplace.catalog.dto;

import com.marketplace.catalog.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull ProductStatus status
) {
}
