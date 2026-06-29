package com.marketplace.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 50) String label,
        @NotBlank @Size(max = 100) String recipientName,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Size(max = 255) String line1,
        @Size(max = 255) String line2,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 20) String postalCode,
        @NotBlank @Size(min = 2, max = 2) String country,
        boolean isDefault
) {
}
