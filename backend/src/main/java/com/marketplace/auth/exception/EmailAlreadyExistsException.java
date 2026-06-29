package com.marketplace.auth.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS", "Email is already registered", HttpStatus.CONFLICT);
    }
}
