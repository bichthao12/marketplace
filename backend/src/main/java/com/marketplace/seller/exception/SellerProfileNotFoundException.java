package com.marketplace.seller.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SellerProfileNotFoundException extends ApiException {

    public SellerProfileNotFoundException() {
        super("SELLER_PROFILE_NOT_FOUND", "Seller profile not found", HttpStatus.NOT_FOUND);
    }
}
