package com.marketplace.order.service;

import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.common.exception.ApiException;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.order.dto.OrderDetailResponse;
import com.marketplace.order.dto.SellerOrderGroupSummary;
import com.marketplace.order.dto.UpdateOrderGroupRequest;
import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderGroup;
import com.marketplace.order.entity.OrderGroupStatus;
import com.marketplace.order.entity.OrderItem;
import com.marketplace.order.entity.Shipment;
import com.marketplace.order.repository.OrderGroupRepository;
import com.marketplace.order.repository.OrderItemRepository;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.repository.ShipmentRepository;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.service.SellerContextService;
import com.marketplace.user.entity.User;
import com.marketplace.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerOrderService {

    private final SellerContextService sellerContextService;
    private final OrderGroupRepository orderGroupRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final UserService userService;

    public SellerOrderService(
            SellerContextService sellerContextService,
            OrderGroupRepository orderGroupRepository,
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            PaymentRepository paymentRepository,
            OrderService orderService,
            UserService userService
    ) {
        this.sellerContextService = sellerContextService;
        this.orderGroupRepository = orderGroupRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SellerOrderGroupSummary> list(OrderGroupStatus status, Pageable pageable) {
        SellerProfile seller = sellerContextService.requireCurrentSellerProfile();
        Page<OrderGroup> page = status == null
                ? orderGroupRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId(), pageable)
                : orderGroupRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(seller.getId(), status, pageable);
        return PageResponse.from(page.map(group -> toSummary(group)));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getGroup(UUID groupId) {
        SellerProfile seller = sellerContextService.requireCurrentSellerProfile();
        OrderGroup group = orderGroupRepository.findByIdAndSellerId(groupId, seller.getId())
                .orElseThrow(() -> new ApiException("ORDER_GROUP_NOT_FOUND", "Order group not found", HttpStatus.NOT_FOUND));
        Order order = orderRepository.findById(group.getOrderId())
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        return orderService.toDetail(order);
    }

    @Transactional
    public OrderDetailResponse updateGroup(UUID groupId, UpdateOrderGroupRequest request) {
        SellerProfile seller = sellerContextService.requireCurrentSellerProfile();
        OrderGroup group = orderGroupRepository.findByIdAndSellerId(groupId, seller.getId())
                .orElseThrow(() -> new ApiException("ORDER_GROUP_NOT_FOUND", "Order group not found", HttpStatus.NOT_FOUND));

        OrderGroupStatus newStatus = parseStatus(request.status());
        validateTransition(group.getStatus(), newStatus, request);

        if (newStatus == OrderGroupStatus.SHIPPED) {
            Shipment shipment = shipmentRepository.findByOrderGroupId(groupId).orElse(new Shipment());
            shipment.setOrderGroupId(groupId);
            shipment.setCarrier(request.shipment().carrier());
            shipment.setTrackingNumber(request.shipment().trackingNumber());
            shipment.setStatus("in_transit");
            shipment.setShippedAt(Instant.now());
            shipmentRepository.save(shipment);
        }

        if (newStatus == OrderGroupStatus.DELIVERED) {
            shipmentRepository.findByOrderGroupId(groupId).ifPresent(s -> {
                s.setStatus("delivered");
                s.setDeliveredAt(Instant.now());
                shipmentRepository.save(s);
            });
            captureCodPayment(group.getOrderId());
        }

        group.setStatus(newStatus);
        orderGroupRepository.save(group);

        Order order = orderRepository.findById(group.getOrderId()).orElseThrow();
        return orderService.toDetail(order);
    }

    private void captureCodPayment(UUID orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.AWAITING_COD) {
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setPaidAt(Instant.now());
                paymentRepository.save(payment);
            }
        });
    }

    private SellerOrderGroupSummary toSummary(OrderGroup group) {
        Order order = orderRepository.findById(group.getOrderId()).orElse(null);
        List<OrderItem> items = orderItemRepository.findByOrderGroupId(group.getId());
        int itemCount = items.stream().mapToInt(OrderItem::getQuantity).sum();
        User buyer = order != null ? userService.getById(order.getBuyerId()) : null;

        Map<String, String> buyerInfo = buyer == null ? Map.of()
                : Map.of(
                        "fullName", maskName(buyer.getFullName()),
                        "phone", maskPhone(buyer.getPhone())
                );

        return new SellerOrderGroupSummary(
                group.getId(),
                order != null ? order.getOrderNumber() : null,
                EnumFormat.toApi(group.getStatus()),
                group.getSubtotal(),
                group.getShippingFee(),
                itemCount,
                buyerInfo,
                group.getCreatedAt()
        );
    }

    private static String maskName(String name) {
        if (name == null || name.length() <= 2) {
            return name;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return phone;
        }
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 4);
    }

    private OrderGroupStatus parseStatus(String status) {
        try {
            return OrderGroupStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("INVALID_STATUS", "Invalid status: " + status, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateTransition(OrderGroupStatus current, OrderGroupStatus next, UpdateOrderGroupRequest request) {
        boolean valid = switch (current) {
            case NEW -> next == OrderGroupStatus.PROCESSING || next == OrderGroupStatus.CANCELLED;
            case PROCESSING -> next == OrderGroupStatus.SHIPPED || next == OrderGroupStatus.CANCELLED;
            case SHIPPED -> next == OrderGroupStatus.DELIVERED;
            default -> false;
        };
        if (!valid) {
            throw new ApiException("INVALID_STATUS_TRANSITION", "Invalid status transition", HttpStatus.CONFLICT);
        }
        if (next == OrderGroupStatus.SHIPPED) {
            if (request.shipment() == null || request.shipment().carrier() == null || request.shipment().trackingNumber() == null) {
                throw new ApiException("TRACKING_REQUIRED", "Carrier and tracking number required", HttpStatus.BAD_REQUEST);
            }
        }
    }
}
