package com.marketplace.auth.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TokenInvalidException extends ApiException {

    public TokenInvalidException() {
        super("TOKEN_INVALID", "Token is invalid", HttpStatus.UNAUTHORIZED);
    }

    public TokenInvalidException(String message) {
        super("TOKEN_INVALID", message, HttpStatus.UNAUTHORIZED);
    }
}
