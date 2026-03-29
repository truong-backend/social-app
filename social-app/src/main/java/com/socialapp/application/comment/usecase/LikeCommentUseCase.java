package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.comment.service.CommentDomainService;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class LikeCommentUseCase {

    private final CommentRepository commentRepository;
    private final CommentDomainService commentDomainService;
    private final NotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher realtimePublisher;

    public LikeCommentUseCase(CommentRepository commentRepository, CommentDomainService commentDomainService, NotificationRepository notificationRepository, NotificationDomainService notificationDomainService, RealtimePublisher realtimePublisher) {
        this.commentRepository = commentRepository;
        this.commentDomainService = commentDomainService;
        this.notificationRepository = notificationRepository;
        this.notificationDomainService = notificationDomainService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public CommentResponseDtos.MessageResponse execute(String requesterId, String commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean alreadyLiked = commentRepository.isLikedByUser(requesterId, commentId);
        commentDomainService.validateLike(alreadyLiked);

        comment.onLiked();
        commentRepository.save(comment);

        if (!comment.getAuthorId().equals(requesterId)) {
            Notification noti = notificationDomainService
                    .createLikedCommentNotification(comment.getAuthorId(), requesterId, commentId);
            notificationRepository.save(noti);
            realtimePublisher.publishToUser(comment.getAuthorId(), "NOTIFICATION", noti);
        }

        return new CommentResponseDtos.MessageResponse("Comment liked");
    }
}
