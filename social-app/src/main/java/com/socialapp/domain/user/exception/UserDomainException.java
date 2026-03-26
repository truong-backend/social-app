package com.socialapp.domain.user.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class UserDomainException extends DomainException {
    public UserDomainException(String message) {
        super("POST_ERROR", message);
    }
}