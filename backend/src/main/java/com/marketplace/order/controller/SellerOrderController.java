package com.marketplace.order.controller;

import com.marketplace.common.dto.PageResponse;
import com.marketplace.order.dto.OrderDetailResponse;
import com.marketplace.order.dto.SellerOrderGroupSummary;
import com.marketplace.order.dto.UpdateOrderGroupRequest;
import com.marketplace.order.entity.OrderGroupStatus;
import com.marketplace.order.service.SellerOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/order-groups")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    public SellerOrderController(SellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public PageResponse<SellerOrderGroupSummary> list(
            @RequestParam(required = false) OrderGroupStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return sellerOrderService.list(status, pageable);
    }

    @GetMapping("/{groupId}")
    public OrderDetailResponse get(@PathVariable UUID groupId) {
        return sellerOrderService.getGroup(groupId);
    }

    @PatchMapping("/{groupId}")
    public OrderDetailResponse update(
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateOrderGroupRequest request
    ) {
        return sellerOrderService.updateGroup(groupId, request);
    }
}
