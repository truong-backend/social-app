package com.socialapp.domain.comment.service;

import com.socialapp.domain.comment.exception.CommentDomainException;

/**
 * Domain Service: CommentDomainService
 */
public class CommentDomainService {

    public void validateLike(boolean alreadyLiked) {
        if (alreadyLiked)
            throw new CommentDomainException("You have already liked this comment");
    }

    public void validateUnlike(boolean alreadyLiked) {
        if (!alreadyLiked)
            throw new CommentDomainException("You have not liked this comment");
    }

    public void validateParentComment(boolean parentExists, boolean parentBelongsToPost) {
        if (!parentExists)
            throw new CommentDomainException("Parent comment does not exist");
        if (!parentBelongsToPost)
            throw new CommentDomainException("Parent comment does not belong to this post");
    }
}