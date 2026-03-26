package com.socialapp.domain.comment.entity;

import com.socialapp.domain.comment.exception.CommentDomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity / Aggregate Root: Comment
 *
 * Chịu trách nhiệm:
 *  - Quản lý nội dung bình luận
 *  - Validate quyền chỉnh sửa / xóa
 *  - Quản lý counter (like, reply)
 */
public class Comment {

    // ── Identity ──────────────────────────────────────────────
    private final String id;
    private final String authorId;
    private final String postId;

    // ── Reply reference ───────────────────────────────────────
    private final String repliedToCommentId;   // null nếu là comment gốc

    // ── Content ───────────────────────────────────────────────
    private String content;
    private final List<String> attachedFilePaths;

    // ── Counters ──────────────────────────────────────────────
    private int likeCount;
    private int replyCount;

    // ── Timestamps ────────────────────────────────────────────
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Private constructor ───────────────────────────────────
    private Comment(String id, String authorId, String postId,
                    String repliedToCommentId, String content,
                    List<String> attachedFilePaths,
                    int likeCount, int replyCount,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id                  = id;
        this.authorId            = authorId;
        this.postId              = postId;
        this.repliedToCommentId  = repliedToCommentId;
        this.content             = content;
        this.attachedFilePaths   = new ArrayList<>(attachedFilePaths);
        this.likeCount           = Math.max(0, likeCount);
        this.replyCount          = Math.max(0, replyCount);
        this.createdAt           = createdAt;
        this.updatedAt           = updatedAt;
    }

    // ── Factory Methods ───────────────────────────────────────

    public static Comment create(String authorId, String postId,
                                 String content, List<String> filePaths) {
        return new Comment(UUID.randomUUID().toString(), authorId, postId,
                null, content,
                filePaths != null ? filePaths : List.of(),
                0, 0,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Comment createReply(String authorId, String postId,
                                      String repliedToCommentId,
                                      String content, List<String> filePaths) {
        if (repliedToCommentId == null || repliedToCommentId.isBlank())
            throw new CommentDomainException("repliedToCommentId must not be blank for a reply");
        return new Comment(UUID.randomUUID().toString(), authorId, postId,
                repliedToCommentId, content,
                filePaths != null ? filePaths : List.of(),
                0, 0,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static Comment reconstitute(String id, String authorId, String postId,
                                       String repliedToCommentId, String content,
                                       List<String> attachedFilePaths,
                                       int likeCount, int replyCount,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Comment(id, authorId, postId, repliedToCommentId, content,
                attachedFilePaths, likeCount, replyCount, createdAt, updatedAt);
    }

    // ── Domain Behaviors ──────────────────────────────────────

    public void updateContent(String requesterId, String newContent,
                              List<String> newFilePaths) {
        validateAuthor(requesterId);
        this.content = newContent;
        this.attachedFilePaths.clear();
        this.attachedFilePaths.addAll(newFilePaths != null ? newFilePaths : List.of());
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(String requesterId, boolean isAdmin) {
        if (!isAdmin) validateAuthor(requesterId);
    }

    public void onLiked()        { this.likeCount  = Math.max(0, likeCount + 1); }
    public void onUnliked()      { this.likeCount  = Math.max(0, likeCount - 1); }
    public void onReplyAdded()   { this.replyCount = Math.max(0, replyCount + 1); }
    public void onReplyRemoved() { this.replyCount = Math.max(0, replyCount - 1); }

    // ── Queries ───────────────────────────────────────────────

    public boolean isReply() { return repliedToCommentId != null; }

    private void validateAuthor(String requesterId) {
        if (!authorId.equals(requesterId))
            throw new CommentDomainException("Only the author can perform this action");
    }

    // ── Getters ───────────────────────────────────────────────

    public String getId()                      { return id; }
    public String getAuthorId()                { return authorId; }
    public String getPostId()                  { return postId; }
    public String getRepliedToCommentId()      { return repliedToCommentId; }
    public String getContent()                 { return content; }
    public List<String> getAttachedFilePaths() { return Collections.unmodifiableList(attachedFilePaths); }
    public int getLikeCount()                  { return likeCount; }
    public int getReplyCount()                 { return replyCount; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }
}