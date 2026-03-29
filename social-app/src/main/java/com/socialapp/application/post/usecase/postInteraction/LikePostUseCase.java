package com.socialapp.application.post.usecase.postInteraction;

import com.socialapp.application.post.dto.response.PostResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class LikePostUseCase {

    private final PostRepository postRepository;
    private final PostDomainService postDomainService;
    private final NotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher realtimePublisher;

    public LikePostUseCase(PostRepository postRepository, PostDomainService postDomainService, NotificationRepository notificationRepository, NotificationDomainService notificationDomainService, RealtimePublisher realtimePublisher) {
        this.postRepository = postRepository;
        this.postDomainService = postDomainService;
        this.notificationRepository = notificationRepository;
        this.notificationDomainService = notificationDomainService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public PostResponseDtos.MessageResponse execute(String requesterId, String postId) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean alreadyLiked = postRepository.isLikedByUser(requesterId, postId);
        postDomainService.validateLike(alreadyLiked);

        post.onLiked();
        postRepository.save(post);

        // Gửi notification nếu không phải tác giả tự like
        if (!post.getAuthorId().equals(requesterId)) {
            Notification noti = notificationDomainService
                    .createLikedPostNotification(post.getAuthorId(), requesterId, postId);
            notificationRepository.save(noti);
            realtimePublisher.publishToUser(post.getAuthorId(), "NOTIFICATION", noti);
        }

        return new PostResponseDtos.MessageResponse("Post liked");
    }
}
