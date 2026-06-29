package com.marketplace.payment.provider;

import com.marketplace.common.config.PaymentProperties.StripeSettings;
import com.marketplace.order.entity.Order;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;
import com.marketplace.payment.service.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentProvider.class);

    private final StripeSettings stripeSettings;

    public StripePaymentProvider(StripeSettings stripeSettings) {
        this.stripeSettings = stripeSettings;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.STRIPE;
    }

    @Override
    public PaymentInitResult initiate(Payment payment, Order order) {
        if (!stripeSettings.isConfigured()) {
            String mockId = "pi_mock_" + UUID.randomUUID();
            String mockSecret = mockId + "_secret_mock";
            log.info("Stripe not configured; returning mock payment intent for order {}", order.getOrderNumber());
            return new PaymentInitResult(mockId, mockSecret, null, stripeSettings.publishableKey());
        }

        Stripe.apiKey = stripeSettings.apiKey();
        long amountCents = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("orderNumber", order.getOrderNumber())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .build());

            return new PaymentInitResult(
                    intent.getId(),
                    intent.getClientSecret(),
                    null,
                    stripeSettings.publishableKey()
            );
        } catch (Exception e) {
            log.error("Stripe payment initiation failed", e);
            String mockId = "pi_mock_" + UUID.randomUUID();
            return new PaymentInitResult(mockId, mockId + "_secret_mock", null, stripeSettings.publishableKey());
        }
    }
}
