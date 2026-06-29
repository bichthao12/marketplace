package com.marketplace.seller.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SellerNotApprovedException extends ApiException {

    public SellerNotApprovedException() {
        super("SELLER_NOT_APPROVED", "Seller account is not approved", HttpStatus.FORBIDDEN);
    }
}
