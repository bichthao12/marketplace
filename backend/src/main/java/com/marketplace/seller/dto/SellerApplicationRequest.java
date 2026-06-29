package com.marketplace.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record SellerApplicationRequest(
        @NotBlank @Size(max = 100) String shopName,
        @Size(max = 2000) String description,
        String businessType,
        String phone,
        Map<String, String> warehouseAddress
) {
}
