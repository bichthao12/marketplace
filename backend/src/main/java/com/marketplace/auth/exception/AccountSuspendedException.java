package com.marketplace.auth.exception;

import com.marketplace.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AccountSuspendedException extends ApiException {

    public AccountSuspendedException() {
        super("ACCOUNT_SUSPENDED", "Account is suspended", HttpStatus.FORBIDDEN);
    }
}
