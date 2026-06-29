package com.marketplace.order.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderGroupRequest(
        @NotBlank String status,
        ShipmentRequest shipment
) {
    public record ShipmentRequest(
            String carrier,
            String trackingNumber
    ) {
    }
}
