package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.request.CommentRequestDtos;
import com.socialapp.application.comment.dto.response.CommentResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
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

@Service
@RequiredArgsConstructor
public class CreateCommentUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher realtimePublisher;

    @Transactional
    public CommentResponseDtos.CommentResponse execute(String authorId, String postId,
                                                       CommentRequestDtos.CreateCommentRequest request,
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

    private CommentResponseDtos.CommentResponse toResponse(Comment c, boolean isLiked) {
        return new CommentResponseDtos.CommentResponse(c.getId(), c.getAuthorId(), null, null,
                c.getPostId(), c.getRepliedToCommentId(), c.getContent(),
                c.getAttachedFilePaths(), c.getLikeCount(), c.getReplyCount(),
                isLiked, c.getCreatedAt(), c.getUpdatedAt());
    }
}
