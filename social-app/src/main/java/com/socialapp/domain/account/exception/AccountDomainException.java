package com.socialapp.domain.account.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class AccountDomainException extends DomainException {
    public AccountDomainException(String message) {
        super("ACCOUNT_ERROR", message);
    }
}
