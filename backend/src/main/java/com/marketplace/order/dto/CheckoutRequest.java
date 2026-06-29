package com.marketplace.order.dto;

import com.marketplace.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CheckoutRequest(
        @NotNull UUID addressId,
        @NotNull PaymentMethod paymentMethod,
        String note
) {
}
