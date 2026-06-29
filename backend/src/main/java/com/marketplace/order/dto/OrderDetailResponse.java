package com.marketplace.order.dto;

import com.marketplace.catalog.dto.SellerSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String orderNumber,
        String status,
        String paymentStatus,
        Map<String, Object> shippingAddress,
        List<OrderGroupDetailResponse> groups,
        PaymentInfoResponse payment,
        BigDecimal subtotal,
        BigDecimal totalShipping,
        BigDecimal grandTotal,
        String currency,
        String note,
        Instant createdAt
) {
    public record OrderGroupDetailResponse(
            UUID id,
            SellerSummaryResponse seller,
            String status,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            List<OrderItemResponse> items,
            ShipmentResponse shipment
    ) {
    }

    public record OrderItemResponse(
            UUID id,
            UUID variantId,
            String productName,
            String sku,
            Map<String, String> variantAttrs,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public record ShipmentResponse(
            String carrier,
            String trackingNumber,
            String status,
            Instant shippedAt,
            Instant deliveredAt
    ) {
    }

    public record PaymentInfoResponse(
            UUID id,
            String method,
            String status,
            Instant paidAt
    ) {
    }
}
