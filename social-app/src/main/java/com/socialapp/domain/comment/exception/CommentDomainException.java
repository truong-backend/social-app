package com.socialapp.domain.comment.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class CommentDomainException extends DomainException {
    public CommentDomainException(String message) {
        super("COMMENT_ERROR", message);
    }
}
