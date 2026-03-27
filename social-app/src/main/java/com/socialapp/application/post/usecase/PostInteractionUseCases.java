package com.socialapp.application.post.usecase;

import com.socialapp.application.post.dto.request.PostRequestDtos.SharePostRequest;
import com.socialapp.application.post.dto.response.PostResponseDtos.MessageResponse;
import com.socialapp.application.post.dto.response.PostResponseDtos.PostResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import com.socialapp.domain.post.valueobject.Privacy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ── LikePostUseCase ─────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class LikePostUseCase {

    private final PostRepository            postRepository;
    private final PostDomainService         postDomainService;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public MessageResponse execute(String requesterId, String postId) {

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

        return new MessageResponse("Post liked");
    }
}

// ── UnlikePostUseCase ───────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UnlikePostUseCase {

    private final PostRepository    postRepository;
    private final PostDomainService postDomainService;

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

@Service
@RequiredArgsConstructor
class SharePostUseCase {

    private final PostRepository    postRepository;
    private final PostDomainService postDomainService;

    @Transactional
    public PostResponse execute(String requesterId, String originalPostId,
                                SharePostRequest request) {

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

        return new PostResponse(
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