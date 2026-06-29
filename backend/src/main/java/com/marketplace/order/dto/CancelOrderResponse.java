package com.marketplace.order.dto;

import java.util.UUID;

public record CancelOrderResponse(
        UUID orderId,
        String status
) {
}
