package com.socialapp.domain.file.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class FileDomainException extends DomainException {
    public FileDomainException(String message) {
        super("FILE_ERROR", message);
    }
}