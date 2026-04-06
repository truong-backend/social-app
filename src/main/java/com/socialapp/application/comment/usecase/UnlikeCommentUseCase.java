package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.comment.service.CommentDomainService;
import org.springframework.transaction.annotation.Transactional;

public class UnlikeCommentUseCase {

    private final CommentRepository    commentRepository;
    private final CommentDomainService commentDomainService;

    public UnlikeCommentUseCase(CommentRepository commentRepository,
                                CommentDomainService commentDomainService) {
        this.commentRepository    = commentRepository;
        this.commentDomainService = commentDomainService;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // ✅ Kiểm tra đã like chưa — nếu chưa sẽ throw từ domain
        boolean alreadyLiked = commentRepository.isLikedByUser(requesterId, commentId);
        commentDomainService.validateUnlike(alreadyLiked);

        // Giảm likeCount trên node
        comment.onUnliked();
        commentRepository.save(comment);

        // ✅ FIX: Xóa relationship (User)-[:LIKED]->(Comment) khỏi graph
        commentRepository.removeLike(requesterId, commentId);

        return new MessageResponse("Comment unliked");
    }
}
