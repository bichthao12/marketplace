package com.marketplace.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminSellerActionRequest(
        @NotBlank String action,
        String reason
) {
}
