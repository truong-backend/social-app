package com.socialapp.application.post.usecase.postInteraction;

import com.socialapp.application.post.dto.response.PostResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── LikePostUseCase ─────────────────────────────────────────────────────────

// ── UnlikePostUseCase ───────────────────────────────────────────────────────

public class UnlikePostUseCase {

    private final PostRepository    postRepository;
    private final PostDomainService postDomainService;

    public UnlikePostUseCase(PostRepository postRepository, PostDomainService postDomainService) {
        this.postRepository = postRepository;
        this.postDomainService = postDomainService;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String postId) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean alreadyLiked = postRepository.isLikedByUser(requesterId, postId);
        postDomainService.validateUnlike(alreadyLiked);

        post.onUnliked();
        postRepository.save(post);

        return new MessageResponse("Post unliked");
    }
}

// ── SharePostUseCase ────────────────────────────────────────────────────────

