package com.socialapp.application.usecase.post;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class DeletePostUseCase {
    private final PostDomainService postDomainService;

    public DeletePostUseCase(PostDomainService postDomainService) {
        this.postDomainService = postDomainService;
    }

    public void execute(String userId, String postId) {
        postDomainService.deletePost(postId, new UserId(userId));
    }
}