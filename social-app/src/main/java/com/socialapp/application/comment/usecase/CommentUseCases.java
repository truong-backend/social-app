package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.request.CommentRequestDtos.*;
import com.socialapp.application.comment.dto.response.CommentResponseDtos.*;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.comment.service.CommentDomainService;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

// ── CreateCommentUseCase ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class CreateCommentUseCase {

    private final CommentRepository         commentRepository;
    private final PostRepository            postRepository;
    private final FileStorage               fileStorage;
    private final FileRepository            fileRepository;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public CommentResponse execute(String authorId, String postId,
                                   CreateCommentRequest request,
                                   List<MultipartFile> files) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        List<String> paths = uploadFiles(files);
        Comment comment = Comment.create(authorId, postId, request.content(), paths);
        commentRepository.save(comment);

        // Tăng comment count trên post
        post.onCommentAdded();
        postRepository.save(post);

        // Notify tác giả bài viết
        if (!post.getAuthorId().equals(authorId)) {
            Notification noti = notificationDomainService
                    .createCommentedPostNotification(post.getAuthorId(), authorId, postId);
            notificationRepository.save(noti);
            realtimePublisher.publishToUser(post.getAuthorId(), "NOTIFICATION", noti);
        }

        return toResponse(comment, false);
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;
        for (MultipartFile f : files) {
            String path = fileStorage.upload(f);
            fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
            paths.add(path);
        }
        return paths;
    }

    private CommentResponse toResponse(Comment c, boolean isLiked) {
        return new CommentResponse(c.getId(), c.getAuthorId(), null, null,
                c.getPostId(), c.getRepliedToCommentId(), c.getContent(),
                c.getAttachedFilePaths(), c.getLikeCount(), c.getReplyCount(),
                isLiked, c.getCreatedAt(), c.getUpdatedAt());
    }
}

// ── ReplyCommentUseCase ──────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class ReplyCommentUseCase {

    private final CommentRepository         commentRepository;
    private final PostRepository            postRepository;
    private final CommentDomainService      commentDomainService;
    private final FileStorage               fileStorage;
    private final FileRepository            fileRepository;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public CommentResponse execute(String authorId, String postId,
                                   String parentCommentId,
                                   ReplyCommentRequest request,
                                   List<MultipartFile> files) {

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        commentDomainService.validateParentComment(true, parent.getPostId().equals(postId));

        List<String> paths = uploadFiles(files);
        Comment reply = Comment.createReply(authorId, postId, parentCommentId,
                request.content(), paths);
        commentRepository.save(reply);

        // Tăng replyCount
        parent.onReplyAdded();
        commentRepository.save(parent);

        // Notify tác giả comment cha
        if (!parent.getAuthorId().equals(authorId)) {
            Notification noti = notificationDomainService
                    .createRepliedCommentNotification(parent.getAuthorId(), authorId, parentCommentId);
            notificationRepository.save(noti);
            realtimePublisher.publishToUser(parent.getAuthorId(), "NOTIFICATION", noti);
        }

        return toResponse(reply, false);
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        if (files == null) return paths;
        for (MultipartFile f : files) {
            String path = fileStorage.upload(f);
            fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
            paths.add(path);
        }
        return paths;
    }

    private CommentResponse toResponse(Comment c, boolean isLiked) {
        return new CommentResponse(c.getId(), c.getAuthorId(), null, null,
                c.getPostId(), c.getRepliedToCommentId(), c.getContent(),
                c.getAttachedFilePaths(), c.getLikeCount(), c.getReplyCount(),
                isLiked, c.getCreatedAt(), c.getUpdatedAt());
    }
}

// ── UpdateCommentUseCase ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UpdateCommentUseCase {

    private final CommentRepository commentRepository;
    private final FileStorage       fileStorage;
    private final FileRepository    fileRepository;

    @Transactional
    public CommentResponse execute(String requesterId, String commentId,
                                   UpdateCommentRequest request,
                                   List<MultipartFile> newFiles) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        fileStorage.deleteAll(comment.getAttachedFilePaths());
        fileRepository.deleteByPaths(comment.getAttachedFilePaths());

        List<String> paths = new ArrayList<>();
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                String path = fileStorage.upload(f);
                fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
                paths.add(path);
            }
        }

        comment.updateContent(requesterId, request.content(), paths);
        commentRepository.save(comment);

        return new CommentResponse(comment.getId(), comment.getAuthorId(), null, null,
                comment.getPostId(), comment.getRepliedToCommentId(), comment.getContent(),
                comment.getAttachedFilePaths(), comment.getLikeCount(), comment.getReplyCount(),
                false, comment.getCreatedAt(), comment.getUpdatedAt());
    }
}

// ── DeleteCommentUseCase ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository    postRepository;
    private final FileStorage       fileStorage;
    private final FileRepository    fileRepository;

    @Transactional
    public MessageResponse execute(String requesterId, String commentId, boolean isAdmin) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.delete(requesterId, isAdmin);

        fileStorage.deleteAll(comment.getAttachedFilePaths());
        fileRepository.deleteByPaths(comment.getAttachedFilePaths());
        commentRepository.deleteById(commentId);

        // Giảm commentCount trên post
        postRepository.findByIdNotDeleted(comment.getPostId()).ifPresent(post -> {
            post.onCommentRemoved();
            postRepository.save(post);
        });

        return new MessageResponse("Comment deleted successfully");
    }
}

// ── LikeCommentUseCase ───────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class LikeCommentUseCase {

    private final CommentRepository         commentRepository;
    private final CommentDomainService      commentDomainService;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public MessageResponse execute(String requesterId, String commentId) {

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

        return new MessageResponse("Comment liked");
    }
}

// ── UnlikeCommentUseCase ─────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UnlikeCommentUseCase {

    private final CommentRepository    commentRepository;
    private final CommentDomainService commentDomainService;

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
