package com.socialapp.application.usecase.comment;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class DeleteCommentUseCase {

    private final PostDomainService postDomainService;

    public DeleteCommentUseCase(PostDomainService postDomainService) {
        this.postDomainService = postDomainService;
    }

    public void execute(String userId, String postId, String commentId) {
        postDomainService.deleteComment(postId, commentId, new UserId(userId));
    }
}