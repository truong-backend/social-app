package com.socialapp.application.usecase.comment;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;



public class LikeCommentUseCase {

    private final PostDomainService postDomainService;

    public LikeCommentUseCase(PostDomainService postDomainService) {
        this.postDomainService = postDomainService;
    }

    public void like(String userId, String postId, String commentId) {
        postDomainService.likeComment(postId, commentId, new UserId(userId));
    }

    public void unlike(String userId, String postId, String commentId) {
        postDomainService.unlikeComment(postId, commentId, new UserId(userId));
    }
}