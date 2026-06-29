package com.marketplace.auth.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ApiException {

    public TokenExpiredException() {
        super("TOKEN_EXPIRED", "Token has expired", HttpStatus.UNAUTHORIZED);
    }
}
