package com.socialapp.domain.message.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class MessageDomainException extends DomainException {
    public MessageDomainException(String message) {
        super("MESSAGE_ERROR", message);
    }
}