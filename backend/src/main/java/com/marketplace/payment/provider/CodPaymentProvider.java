package com.marketplace.payment.provider;

import com.marketplace.order.entity.Order;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;
import com.marketplace.payment.service.PaymentProvider;
import org.springframework.stereotype.Component;

@Component
public class CodPaymentProvider implements PaymentProvider {

    @Override
    public PaymentMethod method() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentInitResult initiate(Payment payment, Order order) {
        return PaymentInitResult.empty();
    }
}
