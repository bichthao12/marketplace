package com.marketplace.catalog.dto;

import com.marketplace.catalog.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 10000) String description,
        @NotNull UUID categoryId,
        @NotEmpty @Valid List<CreateVariantRequest> variants
) {
    public record CreateVariantRequest(
            @NotBlank @Size(max = 64) String sku,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @DecimalMin("0.01") BigDecimal compareAtPrice,
            @NotNull Map<String, String> attributes,
            @NotNull @DecimalMin("0") Integer quantity
    ) {
    }
}
