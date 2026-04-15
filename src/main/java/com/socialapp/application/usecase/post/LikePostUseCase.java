package com.socialapp.application.usecase.post;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class LikePostUseCase {
    private final PostDomainService postDomainService;

    public LikePostUseCase(PostDomainService postDomainService) {
        this.postDomainService = postDomainService;
    }

    public void like(String userId, String postId) {
        postDomainService.likePost(postId, new UserId(userId));
    }

    public void unlike(String userId, String postId) {
        postDomainService.unlikePost(postId, new UserId(userId));
    }
}