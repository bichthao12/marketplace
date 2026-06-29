package com.marketplace.cart.dto;

import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(
        @NotBlank String guestSessionId
) {
}
