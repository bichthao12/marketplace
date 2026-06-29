package com.marketplace.order.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.catalog.dto.SellerSummaryResponse;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.common.exception.ApiException;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.order.dto.CancelOrderResponse;
import com.marketplace.order.dto.OrderDetailResponse;
import com.marketplace.order.dto.OrderSummaryResponse;
import com.marketplace.order.dto.PaymentRetryResponse;
import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderGroup;
import com.marketplace.order.entity.OrderGroupStatus;
import com.marketplace.order.entity.OrderItem;
import com.marketplace.order.entity.OrderStatus;
import com.marketplace.order.entity.Shipment;
import com.marketplace.order.repository.OrderGroupRepository;
import com.marketplace.order.repository.OrderItemRepository;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.repository.ShipmentRepository;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.payment.service.PaymentService;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.repository.SellerProfileRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            OrderGroupRepository orderGroupRepository,
            OrderItemRepository orderItemRepository,
            ShipmentRepository shipmentRepository,
            PaymentRepository paymentRepository,
            SellerProfileRepository sellerProfileRepository,
            PaymentService paymentService,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.orderItemRepository = orderItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentRepository = paymentRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> list(OrderStatus status, Pageable pageable) {
        UUID buyerId = currentUserId();
        Page<Order> page = status == null
                ? orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable)
                : orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, status, pageable);
        return PageResponse.from(page.map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse get(UUID orderId) {
        return getDetail(orderId, currentUserId());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getDetail(UUID orderId, UUID buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        return toDetail(order);
    }

    @Transactional(readOnly = true)
    public PaymentRetryResponse getPayment(UUID orderId) {
        UUID buyerId = currentUserId();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ApiException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException("PAYMENT_NOT_RETRYABLE", "Payment cannot be retried", HttpStatus.BAD_REQUEST);
        }

        PaymentService.PaymentInitResult initResult = paymentService.initiate(payment, order);
        payment.setRedirectUrl(initResult.redirectUrl());
        payment.setClientSecret(initResult.clientSecret());
        payment.setExternalId(initResult.externalId());
        paymentRepository.save(payment);

        return new PaymentRetryResponse(
                payment.getId(),
                EnumFormat.toApi(payment.getMethod()),
                EnumFormat.toApi(payment.getStatus()),
                payment.getRedirectUrl(),
                payment.getClientSecret(),
                initResult.publishableKey(),
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );
    }

    @Transactional
    public CancelOrderResponse cancel(UUID orderId, String reason) {
        UUID buyerId = currentUserId();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        List<OrderGroup> groups = orderGroupRepository.findByOrderId(orderId);
        boolean cancellable = groups.stream().allMatch(g -> g.getStatus() == OrderGroupStatus.NEW);
        if (!cancellable) {
            throw new ApiException("ORDER_NOT_CANCELLABLE", "Order cannot be cancelled", HttpStatus.CONFLICT);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        for (OrderGroup group : groups) {
            group.setStatus(OrderGroupStatus.CANCELLED);
            orderGroupRepository.save(group);
        }

        return new CancelOrderResponse(orderId, EnumFormat.toApi(OrderStatus.CANCELLED));
    }

    @Transactional
    public void cancelStalePendingOrders(Instant before) {
        List<Order> stale = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, before);
        for (Order order : stale) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        }
    }

    private OrderSummaryResponse toSummary(Order order) {
        List<UUID> groupIds = orderGroupRepository.findByOrderId(order.getId()).stream()
                .map(OrderGroup::getId).toList();
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

    OrderDetailResponse toDetail(Order order) {
        List<OrderGroup> groups = orderGroupRepository.findByOrderId(order.getId());
        List<UUID> groupIds = groups.stream().map(OrderGroup::getId).toList();
        Map<UUID, List<OrderItem>> itemsByGroup = orderItemRepository.findByOrderGroupIdIn(groupIds).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderGroupId));
        Map<UUID, Shipment> shipments = shipmentRepository.findByOrderGroupIdIn(groupIds).stream()
                .collect(Collectors.toMap(Shipment::getOrderGroupId, Function.identity()));
        Map<UUID, SellerProfile> sellers = sellerProfileRepository.findAllById(
                groups.stream().map(OrderGroup::getSellerId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(SellerProfile::getId, Function.identity()));

        List<OrderDetailResponse.OrderGroupDetailResponse> groupResponses = groups.stream()
                .map(group -> {
                    SellerSummaryResponse seller = CatalogMapper.toSellerSummary(sellers.get(group.getSellerId()));
                    List<OrderDetailResponse.OrderItemResponse> items = itemsByGroup.getOrDefault(group.getId(), List.of()).stream()
                            .map(item -> new OrderDetailResponse.OrderItemResponse(
                                    item.getId(), item.getVariantId(), item.getProductName(), item.getSku(),
                                    item.getVariantAttrs(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal()
                            )).toList();
                    Shipment shipment = shipments.get(group.getId());
                    OrderDetailResponse.ShipmentResponse shipmentResponse = shipment == null ? null
                            : new OrderDetailResponse.ShipmentResponse(
                                    shipment.getCarrier(), shipment.getTrackingNumber(), shipment.getStatus(),
                                    shipment.getShippedAt(), shipment.getDeliveredAt()
                            );
                    return new OrderDetailResponse.OrderGroupDetailResponse(
                            group.getId(), seller, EnumFormat.toApi(group.getStatus()),
                            group.getSubtotal(), group.getShippingFee(), items, shipmentResponse
                    );
                }).toList();

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        OrderDetailResponse.PaymentInfoResponse paymentInfo = payment == null ? null
                : new OrderDetailResponse.PaymentInfoResponse(
                        payment.getId(), EnumFormat.toApi(payment.getMethod()),
                        EnumFormat.toApi(payment.getStatus()), payment.getPaidAt()
                );

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                EnumFormat.toApi(order.getStatus()),
                EnumFormat.toApi(order.getPaymentStatus()),
                parseAddress(order.getShippingAddressJson()),
                groupResponses,
                paymentInfo,
                order.getSubtotal(),
                order.getTotalShipping(),
                order.getGrandTotal(),
                order.getCurrency(),
                order.getNote(),
                order.getCreatedAt()
        );
    }

    private Map<String, Object> parseAddress(String json) {
        if (json == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static UUID currentUserId() {
        return SecurityUtils.currentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED));
    }
}
