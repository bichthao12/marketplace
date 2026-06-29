package com.marketplace.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.catalog.dto.SellerSummaryResponse;
import com.marketplace.catalog.entity.Inventory;
import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductStatus;
import com.marketplace.catalog.entity.ProductVariant;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.InventoryRepository;
import com.marketplace.catalog.repository.ProductVariantRepository;
import com.marketplace.common.config.CommerceProperties.CommerceSettings;
import com.marketplace.common.exception.ApiException;
import com.marketplace.cart.dto.AddCartItemRequest;
import com.marketplace.cart.dto.CartResponse;
import com.marketplace.cart.dto.MergeCartRequest;
import com.marketplace.cart.dto.UpdateCartItemRequest;
import com.marketplace.cart.model.CartData;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.util.CartSessionResolver;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CartSessionResolver sessionResolver;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final CommerceSettings commerceSettings;

    public CartService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            CartSessionResolver sessionResolver,
            ProductVariantRepository variantRepository,
            InventoryRepository inventoryRepository,
            CommerceSettings commerceSettings
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.sessionResolver = sessionResolver;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.commerceSettings = commerceSettings;
    }

    public CartResponse getCart() {
        CartData cart = loadCart();
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        ProductVariant variant = variantRepository.findById(request.variantId())
                .orElseThrow(() -> new ApiException("VARIANT_NOT_FOUND", "Variant not found", HttpStatus.NOT_FOUND));

        Product product = variant.getProduct();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ApiException("PRODUCT_NOT_AVAILABLE", "Product is not available", HttpStatus.BAD_REQUEST);
        }

        CartData cart = loadCart();
        if (cart.getCurrency() != null && !cart.getCurrency().equals(product.getCurrency())) {
            throw new ApiException("CART_CURRENCY_MISMATCH", "Cannot mix currencies in cart", HttpStatus.BAD_REQUEST);
        }
        if (cart.getCurrency() == null) {
            cart.setCurrency(product.getCurrency());
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getVariantId().equals(request.variantId()))
                .findFirst();

        int newQty = existing.map(i -> i.getQuantity() + request.quantity()).orElse(request.quantity());
        int delta = existing.map(i -> request.quantity()).orElse(request.quantity());

        reserveStock(request.variantId(), delta);

        Instant reservedUntil = Instant.now().plus(commerceSettings.cartReservationMinutes(), ChronoUnit.MINUTES);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(newQty);
            item.setReservedUntil(reservedUntil);
        } else {
            cart.getItems().add(new CartItem(UUID.randomUUID(), request.variantId(), request.quantity(), reservedUntil));
        }

        saveCart(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(UUID itemId, UpdateCartItemRequest request) {
        CartData cart = loadCart();
        CartItem item = findItem(cart, itemId);
        int oldQty = item.getQuantity();
        int newQty = request.quantity();

        if (newQty == 0) {
            releaseStock(item.getVariantId(), oldQty);
            cart.getItems().remove(item);
        } else {
            int delta = newQty - oldQty;
            if (delta > 0) {
                reserveStock(item.getVariantId(), delta);
            } else if (delta < 0) {
                releaseStock(item.getVariantId(), -delta);
            }
            item.setQuantity(newQty);
            item.setReservedUntil(Instant.now().plus(commerceSettings.cartReservationMinutes(), ChronoUnit.MINUTES));
        }

        saveCart(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID itemId) {
        CartData cart = loadCart();
        CartItem item = findItem(cart, itemId);
        releaseStock(item.getVariantId(), item.getQuantity());
        cart.getItems().remove(item);
        saveCart(cart);
        return buildResponse(cart);
    }

    public void clearCart() {
        CartData cart = loadCart();
        for (CartItem item : cart.getItems()) {
            releaseStock(item.getVariantId(), item.getQuantity());
        }
        redisTemplate.delete(sessionResolver.cartKey());
    }

    @Transactional
    public CartResponse merge(MergeCartRequest request) {
        UUID userId = SecurityUtils.currentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED));

        String guestKey = "cart:guest:" + request.guestSessionId().trim();
        CartData guestCart = loadByKey(guestKey);
        if (guestCart.getItems().isEmpty()) {
            return getCart();
        }

        String userKey = "cart:user:" + userId;
        CartData userCart = loadByKey(userKey);

        if (userCart.getCurrency() != null && guestCart.getCurrency() != null
                && !userCart.getCurrency().equals(guestCart.getCurrency())) {
            redisTemplate.delete(guestKey);
            return buildResponse(userCart);
        }

        if (userCart.getCurrency() == null) {
            userCart.setCurrency(guestCart.getCurrency());
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existing = userCart.getItems().stream()
                    .filter(i -> i.getVariantId().equals(guestItem.getVariantId()))
                    .findFirst();
            if (existing.isPresent()) {
                CartItem userItem = existing.get();
                int maxQty = Math.max(userItem.getQuantity(), guestItem.getQuantity());
                int delta = maxQty - userItem.getQuantity();
                if (delta > 0) {
                    reserveStock(guestItem.getVariantId(), delta);
                }
                userItem.setQuantity(maxQty);
            } else {
                userCart.getItems().add(guestItem);
            }
        }

        saveByKey(userKey, userCart);
        redisTemplate.delete(guestKey);
        return buildResponse(userCart);
    }

    public CartData loadCartForCheckout(UUID userId) {
        return loadByKey("cart:user:" + userId);
    }

    public void clearCartForUser(UUID userId) {
        CartData cart = loadByKey("cart:user:" + userId);
        for (CartItem item : cart.getItems()) {
            releaseStock(item.getVariantId(), item.getQuantity());
        }
        redisTemplate.delete("cart:user:" + userId);
    }

    public void releaseExpiredReservations() {
        var keys = redisTemplate.keys("cart:*");
        if (keys == null) {
            return;
        }
        Instant now = Instant.now();
        for (String key : keys) {
            CartData cart = loadByKey(key);
            List<CartItem> expired = cart.getItems().stream()
                    .filter(i -> i.getReservedUntil() != null && i.getReservedUntil().isBefore(now))
                    .toList();
            if (expired.isEmpty()) {
                continue;
            }
            for (CartItem item : expired) {
                releaseStock(item.getVariantId(), item.getQuantity());
                cart.getItems().remove(item);
            }
            if (cart.getItems().isEmpty()) {
                redisTemplate.delete(key);
            } else {
                saveByKey(key, cart);
            }
        }
    }

    private CartItem findItem(CartData cart, UUID itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException("CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND));
    }

    private void reserveStock(UUID variantId, int qty) {
        int updated = inventoryRepository.incrementReserved(variantId, qty);
        if (updated == 0) {
            throw new ApiException("INSUFFICIENT_STOCK", "Insufficient stock", HttpStatus.CONFLICT);
        }
    }

    private void releaseStock(UUID variantId, int qty) {
        if (qty > 0) {
            inventoryRepository.adjustReserved(variantId, -qty);
        }
    }

    private CartData loadCart() {
        return loadByKey(sessionResolver.cartKey());
    }

    private CartData loadByKey(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            CartData cart = new CartData();
            cart.setId(UUID.randomUUID());
            return cart;
        }
        try {
            return objectMapper.readValue(json, CartData.class);
        } catch (JsonProcessingException e) {
            CartData cart = new CartData();
            cart.setId(UUID.randomUUID());
            return cart;
        }
    }

    private void saveCart(CartData cart) {
        saveByKey(sessionResolver.cartKey(), cart);
    }

    private void saveByKey(String key, CartData cart) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(cart));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize cart", e);
        }
    }

    private CartResponse buildResponse(CartData cart) {
        if (cart.getItems().isEmpty()) {
            return new CartResponse(cart.getId(), cart.getCurrency(), 0, BigDecimal.ZERO, List.of(), List.of());
        }

        List<UUID> variantIds = cart.getItems().stream().map(CartItem::getVariantId).toList();
        Map<UUID, ProductVariant> variants = variantRepository.findByIdInWithProduct(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));
        Map<UUID, Inventory> inventoryMap = inventoryRepository.findByVariantIdIn(variantIds).stream()
                .collect(Collectors.toMap(Inventory::getVariantId, Function.identity()));

        Map<UUID, List<CartResponse.CartItemResponse>> bySeller = new LinkedHashMap<>();
        Map<UUID, SellerSummaryResponse> sellerById = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        List<String> warnings = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = variants.get(cartItem.getVariantId());
            if (variant == null) {
                warnings.add("Variant " + cartItem.getVariantId() + " no longer exists");
                continue;
            }
            Product product = variant.getProduct();
            Inventory inv = inventoryMap.get(cartItem.getVariantId());
            int available = inv != null ? inv.getAvailableQty() : 0;

            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            itemCount += cartItem.getQuantity();

            UUID sellerId = product.getSeller().getId();
            sellerById.putIfAbsent(sellerId, CatalogMapper.toSellerSummary(product.getSeller()));

            CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse(
                    cartItem.getId(),
                    cartItem.getVariantId(),
                    product.getName(),
                    variant.getAttributes(),
                    cartItem.getQuantity(),
                    variant.getPrice(),
                    lineTotal,
                    available
            );
            bySeller.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(itemResponse);
        }

        List<CartResponse.CartGroupResponse> groups = bySeller.entrySet().stream()
                .map(entry -> {
                    BigDecimal groupSubtotal = entry.getValue().stream()
                            .map(CartResponse.CartItemResponse::lineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CartResponse.CartGroupResponse(
                            sellerById.get(entry.getKey()),
                            entry.getValue(),
                            groupSubtotal
                    );
                })
                .toList();

        return new CartResponse(cart.getId(), cart.getCurrency(), itemCount, subtotal, groups, warnings);
    }
}
