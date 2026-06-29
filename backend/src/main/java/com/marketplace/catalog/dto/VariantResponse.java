package com.marketplace.catalog.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        String sku,
        Map<String, String> attributes,
        BigDecimal price,
        BigDecimal compareAtPrice,
        int availableQty
) {
}
