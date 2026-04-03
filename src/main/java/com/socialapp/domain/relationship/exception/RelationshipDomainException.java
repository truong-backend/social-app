package com.socialapp.domain.relationship.exception;

import com.socialapp.domain.shared.exception.DomainException;

public class RelationshipDomainException extends DomainException {
    public RelationshipDomainException(String message) {
        super("RELATIONSHIP_ERROR", message);
    }
}
