package com.socialapp.application.usecase.post;

import com.socialapp.application.dto.request.CreatePostRequest;
import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.valueobject.PostContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class CreatePostUseCase {
    private final PostDomainService postDomainService;
    private final PostMapper postMapper;

    public CreatePostUseCase(PostDomainService postDomainService, PostMapper postMapper) {
        this.postDomainService = postDomainService;
        this.postMapper = postMapper;
    }

    public PostResponse execute(String userId, CreatePostRequest req) {
        Post post = postDomainService.createPost(
                new UserId(userId),
                new PostContent(req.content()),
                req.privacy()
        );
        return postMapper.toResponse(post);
    }
}