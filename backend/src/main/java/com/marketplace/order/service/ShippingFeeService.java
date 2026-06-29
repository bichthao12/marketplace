package com.marketplace.order.service;

import com.marketplace.common.config.CommerceProperties.CommerceSettings;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ShippingFeeService {

    private final CommerceSettings commerceSettings;

    public ShippingFeeService(CommerceSettings commerceSettings) {
        this.commerceSettings = commerceSettings;
    }

    public BigDecimal calculateFee(String country, String currency) {
        if ("VN".equalsIgnoreCase(country)) {
            return commerceSettings.vnFlatRate();
        }
        return commerceSettings.globalFlatRate();
    }

    public String shippingMethod(String country) {
        return "VN".equalsIgnoreCase(country) ? "STANDARD_VN" : "STANDARD_INTL";
    }
}
