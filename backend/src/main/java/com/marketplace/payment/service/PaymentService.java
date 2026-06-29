package com.marketplace.payment.service;

import com.marketplace.common.exception.ApiException;
import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderStatus;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;
import com.marketplace.payment.entity.PaymentStatus;
import com.marketplace.payment.repository.PaymentRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final Map<PaymentMethod, PaymentProvider> providers;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(List<PaymentProvider> providerList, PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.providers = new EnumMap<>(PaymentMethod.class);
        for (PaymentProvider provider : providerList) {
            this.providers.put(provider.method(), provider);
        }
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentInitResult initiate(Payment payment, Order order) {
        PaymentProvider provider = providers.get(payment.getMethod());
        if (provider == null) {
            throw new ApiException("PAYMENT_METHOD_NOT_SUPPORTED", "Payment method not supported", HttpStatus.BAD_REQUEST);
        }
        PaymentProvider.PaymentInitResult result = provider.initiate(payment, order);
        return new PaymentInitResult(
                result.externalId(),
                result.clientSecret(),
                result.redirectUrl(),
                result.publishableKey()
        );
    }

    @Transactional
    public void markPaid(String externalId) {
        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ApiException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.CAPTURED) {
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId()).orElseThrow();
        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional
    public void markFailed(String externalId) {
        paymentRepository.findByExternalId(externalId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        });
    }

    public record PaymentInitResult(
            String externalId,
            String clientSecret,
            String redirectUrl,
            String publishableKey
    ) {
    }
}
