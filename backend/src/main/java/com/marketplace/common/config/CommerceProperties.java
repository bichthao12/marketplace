package com.marketplace.common.config;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommerceProperties {

    @Bean
    public CommerceSettings commerceSettings(
            @Value("${app.commerce.cart-reservation-minutes:30}") int cartReservationMinutes,
            @Value("${app.commerce.order-payment-timeout-minutes:15}") int orderPaymentTimeoutMinutes,
            @Value("${app.commerce.shipping.vn-flat-rate:30000}") BigDecimal vnFlatRate,
            @Value("${app.commerce.shipping.global-flat-rate:10.00}") BigDecimal globalFlatRate
    ) {
        return new CommerceSettings(cartReservationMinutes, orderPaymentTimeoutMinutes, vnFlatRate, globalFlatRate);
    }

    public record CommerceSettings(
            int cartReservationMinutes,
            int orderPaymentTimeoutMinutes,
            BigDecimal vnFlatRate,
            BigDecimal globalFlatRate
    ) {
    }
}
