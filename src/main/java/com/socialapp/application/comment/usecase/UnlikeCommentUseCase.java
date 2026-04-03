package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.comment.service.CommentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class UnlikeCommentUseCase {

    private final CommentRepository    commentRepository;
    private final CommentDomainService commentDomainService;

    public UnlikeCommentUseCase(CommentRepository commentRepository, CommentDomainService commentDomainService) {
        this.commentRepository = commentRepository;
        this.commentDomainService = commentDomainService;
    }

    @Transactional
    public MessageResponse execute(String requesterId, String commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean alreadyLiked = commentRepository.isLikedByUser(requesterId, commentId);
        commentDomainService.validateUnlike(alreadyLiked);

        comment.onUnliked();
        commentRepository.save(comment);

        return new MessageResponse("Comment unliked");
    }
}
