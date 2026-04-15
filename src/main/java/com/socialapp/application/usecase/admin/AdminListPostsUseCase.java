package com.socialapp.application.usecase.admin;

import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.PageResponse;
import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.domain.repository.PostRepository;

import java.util.List;

public class AdminListPostsUseCase {

    private final PostRepository postRepository;
    private final PostMapper     postMapper;

    public AdminListPostsUseCase(PostRepository postRepository,
                                 PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper     = postMapper;
    }

    public PageResponse<PostResponse> execute(String keyword, PageRequest page) {
        List<PostResponse> posts = postRepository
                .searchByKeyword(keyword != null ? keyword : "", page.size(), page.offset())
                .stream()
                .map(postMapper::toResponse)
                .toList();
        return PageResponse.of(posts, page.page(), page.size());
    }
}