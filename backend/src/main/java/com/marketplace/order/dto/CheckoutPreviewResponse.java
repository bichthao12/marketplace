package com.marketplace.order.dto;

import com.marketplace.catalog.dto.SellerSummaryResponse;
import com.marketplace.payment.entity.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public record CheckoutPreviewResponse(
        String currency,
        List<PreviewGroup> groups,
        BigDecimal subtotal,
        BigDecimal totalShipping,
        BigDecimal grandTotal,
        List<PaymentMethod> availablePaymentMethods
) {
    public record PreviewGroup(
            SellerSummaryResponse seller,
            List<PreviewItem> items,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            String shippingMethod
    ) {
    }

    public record PreviewItem(
            String productName,
            int quantity,
            BigDecimal lineTotal
    ) {
    }
}
