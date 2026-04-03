package com.socialapp.domain.post.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class PostDomainException extends DomainException {
    public PostDomainException(String message) {
        super("POST_ERROR", message);
    }
}