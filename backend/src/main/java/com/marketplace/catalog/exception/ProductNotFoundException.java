package com.marketplace.catalog.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApiException {

    public ProductNotFoundException() {
        super("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND);
    }
}
