package com.marketplace.payment.service;

import com.marketplace.order.entity.Order;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;

public interface PaymentProvider {

    PaymentMethod method();

    PaymentInitResult initiate(Payment payment, Order order);

    record PaymentInitResult(
            String externalId,
            String clientSecret,
            String redirectUrl,
            String publishableKey
    ) {
        public static PaymentInitResult empty() {
            return new PaymentInitResult(null, null, null, null);
        }
    }
}
