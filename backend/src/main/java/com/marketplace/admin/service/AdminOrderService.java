package com.marketplace.admin.service;

import com.marketplace.common.dto.PageResponse;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.order.dto.OrderSummaryResponse;
import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderItem;
import com.marketplace.order.entity.OrderStatus;
import com.marketplace.order.repository.OrderGroupRepository;
import com.marketplace.order.repository.OrderItemRepository;
import com.marketplace.order.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminOrderService(
            OrderRepository orderRepository,
            OrderGroupRepository orderGroupRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> list(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return PageResponse.from(page.map(this::toSummary));
    }

    private OrderSummaryResponse toSummary(Order order) {
        List<UUID> groupIds = orderGroupRepository.findByOrderId(order.getId()).stream()
                .map(g -> g.getId())
                .toList();
        int itemCount = groupIds.isEmpty() ? 0
                : orderItemRepository.findByOrderGroupIdIn(groupIds).stream().mapToInt(OrderItem::getQuantity).sum();

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                EnumFormat.toApi(order.getStatus()),
                EnumFormat.toApi(order.getPaymentStatus()),
                order.getGrandTotal(),
                order.getCurrency(),
                itemCount,
                order.getCreatedAt()
        );
    }
}
