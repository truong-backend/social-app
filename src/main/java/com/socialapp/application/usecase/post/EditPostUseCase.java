package com.socialapp.application.usecase.post;


import com.socialapp.application.dto.request.EditPostRequest;
import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.valueobject.PostContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.service.PostDomainService;

public class EditPostUseCase {
    private final PostDomainService postDomainService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public EditPostUseCase(PostDomainService postDomainService, PostRepository postRepository, PostMapper postMapper) {
        this.postDomainService = postDomainService;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    public PostResponse execute(String userId, String postId, EditPostRequest req) {
        postDomainService.editPost(postId, new UserId(userId), new PostContent(req.content()));
        postDomainService.changePrivacy(postId, new UserId(userId), req.privacy());
        Post updated = postRepository.findById(postId).orElseThrow();
        return postMapper.toResponse(updated);
    }
}