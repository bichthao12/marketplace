package com.marketplace.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.cart.model.CartData;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.service.CartService;
import com.marketplace.catalog.dto.SellerSummaryResponse;
import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductVariant;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.ProductVariantRepository;
import com.marketplace.common.exception.ApiException;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.order.dto.CheckoutPreviewRequest;
import com.marketplace.order.dto.CheckoutPreviewResponse;
import com.marketplace.order.dto.CheckoutRequest;
import com.marketplace.order.dto.CheckoutResponse;
import com.marketplace.order.dto.OrderDetailResponse;
import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderGroup;
import com.marketplace.order.entity.OrderGroupStatus;
import com.marketplace.order.entity.OrderItem;
import com.marketplace.order.entity.OrderStatus;
import com.marketplace.order.repository.OrderGroupRepository;
import com.marketplace.order.repository.OrderItemRepository;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import com.marketplace.payment.service.PaymentService;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.user.entity.UserAddress;
import com.marketplace.user.service.AddressService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final AddressService addressService;
    private final ProductVariantRepository variantRepository;
    private final ShippingFeeService shippingFeeService;
    private final OrderRepository orderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public CheckoutService(
            CartService cartService,
            AddressService addressService,
            ProductVariantRepository variantRepository,
            ShippingFeeService shippingFeeService,
            OrderRepository orderRepository,
            OrderGroupRepository orderGroupRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService,
            OrderService orderService,
            ObjectMapper objectMapper
    ) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.variantRepository = variantRepository;
        this.shippingFeeService = shippingFeeService;
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CheckoutPreviewResponse preview(CheckoutPreviewRequest request) {
        UUID userId = currentUserId();
        UserAddress address = addressService.getOwnedOrThrow(request.addressId(), userId);
        CartData cart = cartService.loadCartForCheckout(userId);
        if (cart.getItems().isEmpty()) {
            throw new ApiException("CART_EMPTY", "Cart is empty", HttpStatus.BAD_REQUEST);
        }
        return buildPreview(cart, address);
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        UUID userId = currentUserId();
        UserAddress address = addressService.getOwnedOrThrow(request.addressId(), userId);
        CartData cart = cartService.loadCartForCheckout(userId);
        if (cart.getItems().isEmpty()) {
            throw new ApiException("CART_EMPTY", "Cart is empty", HttpStatus.BAD_REQUEST);
        }

        validatePaymentMethod(request.paymentMethod(), address.getCountry());

        CheckoutPreviewResponse preview = buildPreview(cart, address);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBuyerId(userId);
        order.setCurrency(cart.getCurrency());
        order.setSubtotal(preview.subtotal());
        order.setTotalShipping(preview.totalShipping());
        order.setGrandTotal(preview.grandTotal());
        order.setShippingAddressJson(toAddressJson(address));
        order.setNote(request.note());

        if (request.paymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.AWAITING_COD);
        } else {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setPaymentStatus(PaymentStatus.PENDING);
        }
        order = orderRepository.save(order);

        Map<UUID, List<CartItem>> bySeller = groupCartBySeller(cart);
        for (var entry : bySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<CartItem> items = entry.getValue();

            BigDecimal groupSubtotal = BigDecimal.ZERO;
            for (CartItem cartItem : items) {
                ProductVariant variant = variantRepository.findByIdInWithProduct(List.of(cartItem.getVariantId())).getFirst();
                groupSubtotal = groupSubtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }

            BigDecimal shippingFee = shippingFeeService.calculateFee(address.getCountry(), cart.getCurrency());

            OrderGroup group = new OrderGroup();
            group.setOrderId(order.getId());
            group.setSellerId(sellerId);
            group.setSubtotal(groupSubtotal);
            group.setShippingFee(shippingFee);
            group.setStatus(OrderGroupStatus.NEW);
            group = orderGroupRepository.save(group);

            for (CartItem cartItem : items) {
                ProductVariant variant = variantRepository.findByIdInWithProduct(List.of(cartItem.getVariantId())).getFirst();
                Product product = variant.getProduct();

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderGroupId(group.getId());
                orderItem.setVariantId(variant.getId());
                orderItem.setProductName(product.getName());
                orderItem.setSku(variant.getSku());
                orderItem.setVariantAttrs(variant.getAttributes());
                orderItem.setUnitPrice(variant.getPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setLineTotal(variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                orderItemRepository.save(orderItem);
            }
        }

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setMethod(request.paymentMethod());
        payment.setAmount(order.getGrandTotal());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(request.paymentMethod() == PaymentMethod.COD ? PaymentStatus.AWAITING_COD : PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        PaymentService.PaymentInitResult initResult = paymentService.initiate(payment, order);
        payment.setExternalId(initResult.externalId());
        payment.setClientSecret(initResult.clientSecret());
        payment.setRedirectUrl(initResult.redirectUrl());
        paymentRepository.save(payment);

        cartService.clearCartForUser(userId);

        OrderDetailResponse orderDetail = orderService.getDetail(order.getId(), userId);
        CheckoutResponse.PaymentResponse paymentResponse = new CheckoutResponse.PaymentResponse(
                payment.getId(),
                EnumFormat.toApi(payment.getMethod()),
                EnumFormat.toApi(payment.getStatus()),
                payment.getRedirectUrl(),
                payment.getClientSecret(),
                initResult.publishableKey(),
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );

        return new CheckoutResponse(orderDetail, paymentResponse);
    }

    private CheckoutPreviewResponse buildPreview(CartData cart, UserAddress address) {
        Map<UUID, List<CartItem>> bySeller = groupCartBySeller(cart);
        List<CheckoutPreviewResponse.PreviewGroup> groups = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalShipping = BigDecimal.ZERO;

        for (var entry : bySeller.entrySet()) {
            List<CheckoutPreviewResponse.PreviewItem> items = new ArrayList<>();
            BigDecimal groupSubtotal = BigDecimal.ZERO;
            SellerProfile seller = null;

            for (CartItem cartItem : entry.getValue()) {
                ProductVariant variant = variantRepository.findByIdInWithProduct(List.of(cartItem.getVariantId())).getFirst();
                seller = variant.getProduct().getSeller();
                BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                groupSubtotal = groupSubtotal.add(lineTotal);
                items.add(new CheckoutPreviewResponse.PreviewItem(
                        variant.getProduct().getName(),
                        cartItem.getQuantity(),
                        lineTotal
                ));
            }

            BigDecimal shippingFee = shippingFeeService.calculateFee(address.getCountry(), cart.getCurrency());
            subtotal = subtotal.add(groupSubtotal);
            totalShipping = totalShipping.add(shippingFee);

            groups.add(new CheckoutPreviewResponse.PreviewGroup(
                    CatalogMapper.toSellerSummary(seller),
                    items,
                    groupSubtotal,
                    shippingFee,
                    shippingFeeService.shippingMethod(address.getCountry())
            ));
        }

        List<PaymentMethod> methods = availablePaymentMethods(address.getCountry());
        return new CheckoutPreviewResponse(
                cart.getCurrency(),
                groups,
                subtotal,
                totalShipping,
                subtotal.add(totalShipping),
                methods
        );
    }

    private Map<UUID, List<CartItem>> groupCartBySeller(CartData cart) {
        Map<UUID, List<CartItem>> bySeller = new LinkedHashMap<>();
        List<UUID> variantIds = cart.getItems().stream().map(CartItem::getVariantId).toList();
        Map<UUID, ProductVariant> variants = variantRepository.findByIdInWithProduct(variantIds).stream()
                .collect(java.util.stream.Collectors.toMap(ProductVariant::getId, v -> v));

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = variants.get(item.getVariantId());
            if (variant == null) {
                throw new ApiException("VARIANT_NOT_FOUND", "Variant not found in cart", HttpStatus.BAD_REQUEST);
            }
            UUID sellerId = variant.getProduct().getSeller().getId();
            bySeller.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);
        }
        return bySeller;
    }

    private List<PaymentMethod> availablePaymentMethods(String country) {
        List<PaymentMethod> methods = new ArrayList<>();
        if ("VN".equalsIgnoreCase(country)) {
            methods.add(PaymentMethod.VNPAY);
            methods.add(PaymentMethod.COD);
        }
        methods.add(PaymentMethod.STRIPE);
        return methods;
    }

    private void validatePaymentMethod(PaymentMethod method, String country) {
        if (method == PaymentMethod.COD && !"VN".equalsIgnoreCase(country)) {
            throw new ApiException("COD_NOT_AVAILABLE", "COD is only available in Vietnam", HttpStatus.BAD_REQUEST);
        }
    }

    private String toAddressJson(UserAddress address) {
        try {
            Map<String, Object> map = Map.of(
                    "recipientName", address.getRecipientName(),
                    "phone", address.getPhone(),
                    "line1", address.getLine1(),
                    "line2", address.getLine2() != null ? address.getLine2() : "",
                    "city", address.getCity(),
                    "state", address.getState() != null ? address.getState() : "",
                    "postalCode", address.getPostalCode() != null ? address.getPostalCode() : "",
                    "country", address.getCountry()
            );
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize address", e);
        }
    }

    private String generateOrderNumber() {
        String date = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD-" + date + "-" + suffix;
    }

    private static UUID currentUserId() {
        return SecurityUtils.currentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED));
    }
}
