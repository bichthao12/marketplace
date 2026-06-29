package com.marketplace.payment.provider;

import com.marketplace.common.config.PaymentProperties.VnpaySettings;
import com.marketplace.order.entity.Order;
import com.marketplace.payment.entity.Payment;
import com.marketplace.payment.entity.PaymentMethod;
import com.marketplace.payment.service.PaymentProvider;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class VNPayPaymentProvider implements PaymentProvider {

    private final VnpaySettings vnpaySettings;

    public VNPayPaymentProvider(VnpaySettings vnpaySettings) {
        this.vnpaySettings = vnpaySettings;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentInitResult initiate(Payment payment, Order order) {
        String txnRef = order.getOrderNumber() + "-" + UUID.randomUUID().toString().substring(0, 8);
        long amount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpaySettings.tmnCode().isBlank() ? "DEMO" : vnpaySettings.tmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Payment for " + order.getOrderNumber());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnpaySettings.returnUrl());
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(vnpaySettings.hashSecret().isBlank() ? "SECRET" : vnpaySettings.hashSecret(), hashData);
        String redirectUrl = vnpaySettings.url() + "?" + hashData + "&vnp_SecureHash=" + secureHash;

        return new PaymentInitResult(txnRef, null, redirectUrl, null);
    }

    private static String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                hashData.append('=');
                hashData.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                hashData.append('&');
            }
        }
        if (!hashData.isEmpty()) {
            hashData.setLength(hashData.length() - 1);
        }
        return hashData.toString();
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute VNPay hash", e);
        }
    }
}
