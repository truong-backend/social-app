package com.socialapp.application.post.usecase.postMutation;

import com.socialapp.application.post.dto.request.PostRequestDtos.UpdatePostPrivacyRequest;
import com.socialapp.application.post.dto.response.PostResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.valueobject.Privacy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── UpdatePostContentUseCase ────────────────────────────────────────────────

// ── UpdatePostPrivacyUseCase ────────────────────────────────────────────────


public class UpdatePostPrivacyUseCase {

    private final PostRepository postRepository;

    public UpdatePostPrivacyUseCase(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String postId,
                                   UpdatePostPrivacyRequest request) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.updatePrivacy(requesterId, Privacy.valueOf(request.privacy()));
        postRepository.save(post);

        return new MessageResponse("Privacy updated successfully");
    }
}

// ── DeletePostUseCase ───────────────────────────────────────────────────────

