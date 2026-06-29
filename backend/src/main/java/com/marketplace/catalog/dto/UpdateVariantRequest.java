package com.marketplace.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public record UpdateVariantRequest(
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull @DecimalMin("0") Integer quantity,
        Map<String, String> attributes
) {
}
