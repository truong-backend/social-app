package com.socialapp.application.post.usecase.postInteraction;

import com.socialapp.application.post.dto.response.PostResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import org.springframework.transaction.annotation.Transactional;

public class UnlikePostUseCase {

    private final PostRepository    postRepository;
    private final PostDomainService postDomainService;

    public UnlikePostUseCase(PostRepository postRepository, PostDomainService postDomainService) {
        this.postRepository    = postRepository;
        this.postDomainService = postDomainService;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String postId) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // ✅ Kiểm tra đã like chưa — nếu chưa sẽ throw từ domain
        boolean alreadyLiked = postRepository.isLikedByUser(requesterId, postId);
        postDomainService.validateUnlike(alreadyLiked);

        // Giảm likeCount trên node
        post.onUnliked();
        postRepository.save(post);

        // ✅ FIX: Xóa relationship (User)-[:LIKED]->(Post) khỏi graph
        postRepository.removeLike(requesterId, postId);

        return new MessageResponse("Post unliked");
    }
}
