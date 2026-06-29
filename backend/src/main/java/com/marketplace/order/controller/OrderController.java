package com.marketplace.order.controller;

import com.marketplace.common.dto.PageResponse;
import com.marketplace.order.dto.CancelOrderResponse;
import com.marketplace.order.dto.OrderDetailResponse;
import com.marketplace.order.dto.OrderSummaryResponse;
import com.marketplace.order.dto.PaymentRetryResponse;
import com.marketplace.order.entity.OrderStatus;
import com.marketplace.order.service.OrderService;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageResponse<OrderSummaryResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return orderService.list(status, pageable);
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse get(@PathVariable UUID orderId) {
        return orderService.get(orderId);
    }

    @GetMapping("/{orderId}/payment")
    public PaymentRetryResponse getPayment(@PathVariable UUID orderId) {
        return orderService.getPayment(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public CancelOrderResponse cancel(@PathVariable UUID orderId, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return orderService.cancel(orderId, reason);
    }
}
