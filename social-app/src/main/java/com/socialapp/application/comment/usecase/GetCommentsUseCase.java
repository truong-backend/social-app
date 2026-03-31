package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos.CommentResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCommentsUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository    postRepository;

    /**
     * GET /api/posts/{postId}/comments?skip=0&limit=10
     * Trả về top-level comments (không phải replies) theo postId.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> execute(String requesterId, String postId, int skip, int limit) {

        postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        return commentRepository.findByPostId(postId, skip, limit)
                .stream()
                .map(c -> toResponse(c, commentRepository.isLikedByUser(requesterId, c.getId())))
                .toList();
    }

    private CommentResponse toResponse(Comment c, boolean isLiked) {
        return new CommentResponse(
                c.getId(), c.getAuthorId(), null, null,
                c.getPostId(), c.getRepliedToCommentId(),
                c.getContent(), c.getAttachedFilePaths(),
                c.getLikeCount(), c.getReplyCount(),
                isLiked, c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}