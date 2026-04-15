package com.socialapp.domain.service;

import com.socialapp.application.dto.response.ErrorCode;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Keyword;
import com.socialapp.domain.model.entity.Notification;
import com.socialapp.domain.model.valueobject.*;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.presentation.advice.DomainException;

import java.util.List;
import java.util.UUID;

/**
 * Domain Service: PostDomainService
 * ─────────────────────────────────────────────────────────────
 * Xử lý nghiệp vụ bài viết — logic liên quan nhiều Aggregate.
 *
 * Trách nhiệm:
 *   - Tạo / sửa / xóa bài viết
 *   - Đính kèm file vào bài viết
 *   - Like / Unlike bài viết
 *   - Share bài viết (chỉ PUBLIC)
 *   - Thêm / xóa bình luận
 *   - Like / Unlike bình luận
 *   - Gán keyword cho bài viết
 */
public class PostDomainService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostDomainService(PostRepository postRepository,
                             UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // ── Create Post ───────────────────────────────────────────

    public Post createPost(UserId authorId, PostContent content, PostPrivacy privacy) {
        Post post = new Post(UUID.randomUUID().toString(), authorId, content, privacy);
        postRepository.save(post);
        return post;
    }

    // ── Edit Post ─────────────────────────────────────────────

    public void editPost(String postId, UserId requesterId, PostContent newContent) {
        Post post = requirePost(postId);
        assertAuthor(post, requesterId);
        post.editContent(newContent);
        postRepository.save(post);
    }

    // ── Change Privacy ────────────────────────────────────────

    public void changePrivacy(String postId, UserId requesterId, PostPrivacy privacy) {
        Post post = requirePost(postId);
        assertAuthor(post, requesterId);
        post.changePrivacy(privacy);
        postRepository.save(post);
    }

    // ── Delete Post ───────────────────────────────────────────

    public void deletePost(String postId, UserId requesterId) {
        Post post = requirePost(postId);
        assertAuthor(post, requesterId);
        post.softDelete();
        postRepository.save(post);
    }

    // ── Attach File ───────────────────────────────────────────

    public void attachFileToPost(String postId, UserId requesterId, FileMeta fileMeta) {
        Post post = requirePost(postId);
        assertAuthor(post, requesterId);
        post.attachFile(new FileEntity(fileMeta));
        postRepository.save(post);
    }

    // ── Like / Unlike Post ────────────────────────────────────

    public void likePost(String postId, UserId likerId) {
        Post post = requirePost(postId);

        if (postRepository.hasLiked(likerId, postId))
            throw new DomainException(ErrorCode.ALREADY_LIKED,
                    "Bạn đã thích bài viết này rồi");

        post.like();
        postRepository.addLike(likerId, postId);

        if (!post.getAuthorId().equals(likerId)) {
            notifyUser(post.getAuthorId(),
                    NotificationAction.LIKE_POST,
                    NotificationTarget.TargetType.POST,
                    postId);
        }
        postRepository.save(post);
    }

    public void unlikePost(String postId, UserId userId) {
        Post post = requirePost(postId);
        if (!postRepository.hasLiked(userId, postId))
            throw new DomainException(ErrorCode.NOT_LIKED_YET,
                    "Bạn chưa thích bài viết này");

        post.unlike();
        postRepository.removeLike(userId, postId);
        postRepository.save(post);
    }

    // ── Share Post ────────────────────────────────────────────

    public Post sharePost(String originalPostId, UserId sharerId) {
        Post original = requirePost(originalPostId);
        original.incrementShareCount();   // throws IllegalStateException nếu không PUBLIC

        Post shared = new Post(
                UUID.randomUUID().toString(),
                sharerId,
                original.getContent(),
                PostPrivacy.PUBLIC
        );
        shared.setSharedFromPostId(originalPostId);

        postRepository.save(original);
        postRepository.save(shared);
        return shared;
    }

    // ── Comment ───────────────────────────────────────────────

    public Comment addComment(String postId, UserId authorId, CommentContent content) {
        Post post = requirePost(postId);
        Comment comment = new Comment(UUID.randomUUID().toString(), authorId, content);
        post.addComment(comment);

        if (!post.getAuthorId().equals(authorId)) {
            notifyUser(post.getAuthorId(),
                    NotificationAction.COMMENT_POST,
                    NotificationTarget.TargetType.COMMENT,
                    comment.getId());
        }
        postRepository.save(post);
        return comment;
    }

    public void deleteComment(String postId, String commentId, UserId requesterId) {
        Post post = requirePost(postId);
        post.removeComment(commentId);
        postRepository.save(post);
    }

    public void likeComment(String postId, String commentId, UserId likerId) {
        Post post = requirePost(postId);
        post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .ifPresent(c -> {
                    c.like();
                    if (!c.getAuthorId().equals(likerId)) {
                        notifyUser(c.getAuthorId(),
                                NotificationAction.LIKE_COMMENT,
                                NotificationTarget.TargetType.COMMENT,
                                commentId);
                    }
                });
        postRepository.save(post);
    }

    // Thêm method này ngay sau likeComment(...)
    public void unlikeComment(String postId, String commentId, UserId userId) {
        Post post = requirePost(postId);
        post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .ifPresent(c -> c.unlike());
        postRepository.save(post);
    }
    // ── Keywords ──────────────────────────────────────────────

    public void setKeywords(String postId, List<String> keywordTexts) {
        Post post = requirePost(postId);
        List<Keyword> keywords = keywordTexts.stream()
                .map(Keyword::new)
                .toList();
        post.setKeywords(keywords);
        postRepository.save(post);
    }

    // ── Helpers ───────────────────────────────────────────────

    private Post requirePost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.POST_NOT_FOUND, "Post not found: " + postId));
    }

    private void assertAuthor(Post post, UserId requesterId) {
        if (!post.getAuthorId().equals(requesterId))
            throw new DomainException(ErrorCode.NOT_POST_AUTHOR,
                    "Only the author can perform this action");
    }

    private void notifyUser(UserId targetUserId,
                            NotificationAction action,
                            NotificationTarget.TargetType targetType,
                            String targetId) {
        userRepository.findById(targetUserId).ifPresent(user -> {
            Notification notification = new Notification(
                    UUID.randomUUID().toString(),
                    action,
                    new NotificationTarget(targetType, targetId)
            );
            user.addNotification(notification);
            userRepository.save(user);
        });
    }
}