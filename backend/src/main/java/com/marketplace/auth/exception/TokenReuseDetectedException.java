package com.marketplace.auth.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TokenReuseDetectedException extends ApiException {

    public TokenReuseDetectedException() {
        super("TOKEN_REUSE_DETECTED", "Refresh token reuse detected. Please sign in again.", HttpStatus.UNAUTHORIZED);
    }
}
