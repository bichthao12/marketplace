package com.marketplace.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentProperties {

    @Bean
    public StripeSettings stripeSettings(
            @Value("${app.payment.stripe.api-key:}") String apiKey,
            @Value("${app.payment.stripe.webhook-secret:}") String webhookSecret,
            @Value("${app.payment.stripe.publishable-key:}") String publishableKey
    ) {
        return new StripeSettings(apiKey, webhookSecret, publishableKey);
    }

    @Bean
    public VnpaySettings vnpaySettings(
            @Value("${app.payment.vnpay.tmn-code:}") String tmnCode,
            @Value("${app.payment.vnpay.hash-secret:}") String hashSecret,
            @Value("${app.payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") String url,
            @Value("${app.payment.vnpay.return-url:http://localhost:5173/checkout/return}") String returnUrl
    ) {
        return new VnpaySettings(tmnCode, hashSecret, url, returnUrl);
    }

    public record StripeSettings(String apiKey, String webhookSecret, String publishableKey) {
        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public record VnpaySettings(String tmnCode, String hashSecret, String url, String returnUrl) {
        public boolean isConfigured() {
            return tmnCode != null && !tmnCode.isBlank() && hashSecret != null && !hashSecret.isBlank();
        }
    }
}
