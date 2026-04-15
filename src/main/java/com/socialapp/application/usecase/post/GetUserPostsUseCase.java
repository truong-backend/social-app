package com.socialapp.application.usecase.post;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.PostRepository;


import java.util.List;


public class GetUserPostsUseCase {

    private final PostRepository postRepository;
    private final PostMapper     postMapper;

    public GetUserPostsUseCase(PostRepository postRepository,
                               PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper     = postMapper;
    }

    public PageResponse<PostResponse> execute(String targetUserId, PageRequest page) {
        List<PostResponse> posts = postRepository
                .findByAuthorId(new UserId(targetUserId), page.size(), page.offset())
                .stream()
                .map(postMapper::toResponse)
                .toList();
        return PageResponse.of(posts, page.page(), page.size());
    }
}