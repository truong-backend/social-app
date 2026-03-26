package com.socialapp.domain.notification.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class NotificationDomainException extends DomainException {
    public NotificationDomainException(String message) {
        super("NOTIFICATION_ERROR", message);
    }
}
