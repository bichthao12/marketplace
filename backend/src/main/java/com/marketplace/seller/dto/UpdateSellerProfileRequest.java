package com.marketplace.seller.dto;

import jakarta.validation.constraints.Size;

public record UpdateSellerProfileRequest(
        @Size(max = 2000) String description,
        String phone,
        String logoUrl
) {
}
