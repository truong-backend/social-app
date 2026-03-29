package com.socialapp.application.post.usecase.postInteraction;

import com.socialapp.application.post.dto.request.PostRequestDtos;
import com.socialapp.application.post.dto.response.PostResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import com.socialapp.domain.post.valueobject.Privacy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class SharePostUseCase {

    private final PostRepository postRepository;
    private final PostDomainService postDomainService;

    public SharePostUseCase(PostRepository postRepository, PostDomainService postDomainService) {
        this.postRepository = postRepository;
        this.postDomainService = postDomainService;
    }

    @Transactional
    public PostResponseDtos.PostResponse execute(String requesterId, String originalPostId,
                                                 PostRequestDtos.SharePostRequest request) {

        Post original = postRepository.findByIdNotDeleted(originalPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Domain validate
        postDomainService.validateShare(original);

        // Tạo shared post
        Post sharedPost = Post.createShare(
                requesterId, request.content(),
                Privacy.valueOf(request.privacy()),
                originalPostId, original.getPrivacy()
        );
        postRepository.save(sharedPost);

        // Tăng shareCount bài gốc
        original.onShared();
        postRepository.save(original);

        return new PostResponseDtos.PostResponse(
                sharedPost.getId(), sharedPost.getAuthorId(), null, null,
                sharedPost.getContent(), sharedPost.getPrivacy().name(),
                sharedPost.getCounts().getLikeCount(),
                sharedPost.getCounts().getShareCount(),
                sharedPost.getCounts().getCommentCount(),
                false, true, originalPostId,
                sharedPost.getAttachedFilePaths(),
                sharedPost.getCreatedAt(), sharedPost.getUpdatedAt()
        );
    }
}
