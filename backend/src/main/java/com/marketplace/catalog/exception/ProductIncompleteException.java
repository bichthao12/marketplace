package com.marketplace.catalog.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductIncompleteException extends ApiException {

    public ProductIncompleteException(String message) {
        super("PRODUCT_INCOMPLETE", message, HttpStatus.BAD_REQUEST);
    }
}
