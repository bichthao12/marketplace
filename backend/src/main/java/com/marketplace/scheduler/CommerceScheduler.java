package com.marketplace.scheduler;

import com.marketplace.cart.service.CartService;
import com.marketplace.common.config.CommerceProperties.CommerceSettings;
import com.marketplace.order.service.OrderService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CommerceScheduler {

    private static final Logger log = LoggerFactory.getLogger(CommerceScheduler.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final CommerceSettings commerceSettings;

    public CommerceScheduler(CartService cartService, OrderService orderService, CommerceSettings commerceSettings) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.commerceSettings = commerceSettings;
    }

    @Scheduled(fixedRate = 60_000)
    public void releaseExpiredReservations() {
        try {
            cartService.releaseExpiredReservations();
        } catch (Exception e) {
            log.warn("Failed to release expired cart reservations", e);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void cancelStalePendingOrders() {
        try {
            Instant cutoff = Instant.now().minus(commerceSettings.orderPaymentTimeoutMinutes(), ChronoUnit.MINUTES);
            orderService.cancelStalePendingOrders(cutoff);
        } catch (Exception e) {
            log.warn("Failed to cancel stale pending orders", e);
        }
    }
}
