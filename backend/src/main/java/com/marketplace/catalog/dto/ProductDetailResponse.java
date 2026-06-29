package com.marketplace.catalog.dto;

import com.marketplace.catalog.entity.ProductStatus;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String currency,
        ProductStatus status,
        CategoryRefResponse category,
        SellerSummaryResponse seller,
        List<ProductImageResponse> images,
        List<VariantResponse> variants
) {
}
