package com.socialapp.application.usecase.post;


import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class SharePostUseCase {
    private final PostDomainService postDomainService;
    private final PostMapper postMapper;

    public SharePostUseCase(PostDomainService postDomainService, PostMapper postMapper) {
        this.postDomainService = postDomainService;
        this.postMapper = postMapper;
    }

    public PostResponse execute(String userId, String originalPostId) {
        Post shared = postDomainService.sharePost(originalPostId, new UserId(userId));
        return postMapper.toResponse(shared);
    }
}