package com.socialapp.domain.post.entity;

import com.socialapp.domain.post.exception.PostDomainException;
import com.socialapp.domain.post.valueobject.PostCounts;
import com.socialapp.domain.post.valueobject.Privacy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity / Aggregate Root: Post
 *
 * Chịu trách nhiệm:
 *  - Quản lý nội dung và quyền riêng tư bài viết
 *  - Validate quyền chỉnh sửa / xóa (chỉ tác giả hoặc admin)
 *  - Quản lý counter (like, share, comment)
 *  - Validate logic share (bài gốc phải PUBLIC)
 */
public class Post {

    // ── Identity ──────────────────────────────────────────────
    private final String id;
    private final String authorId;

    // ── Content ───────────────────────────────────────────────
    private String content;
    private Privacy privacy;

    // ── Shared post reference ─────────────────────────────────
    private final String sharedFromPostId;  // null nếu không phải share

    // ── Attached files (paths) ────────────────────────────────
    private final List<String> attachedFilePaths;

    // ── Keywords ──────────────────────────────────────────────
    private List<String> keywords;

    // ── Counters ──────────────────────────────────────────────
    private PostCounts counts;

    // ── Timestamps ────────────────────────────────────────────
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // ── Private constructor ───────────────────────────────────
    private Post(String id, String authorId, String content, Privacy privacy,
                 String sharedFromPostId, List<String> attachedFilePaths,
                 List<String> keywords, PostCounts counts,
                 LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id                 = id;
        this.authorId           = authorId;
        this.content            = content;
        this.privacy            = privacy;
        this.sharedFromPostId   = sharedFromPostId;
        this.attachedFilePaths  = new ArrayList<>(attachedFilePaths);
        this.keywords           = new ArrayList<>(keywords);
        this.counts             = counts;
        this.createdAt          = createdAt;
        this.updatedAt          = updatedAt;
        this.deletedAt          = deletedAt;
    }

    // ── Factory Methods ───────────────────────────────────────

    public static Post create(String authorId, String content,
                              Privacy privacy, List<String> filePaths) {
        return new Post(
                UUID.randomUUID().toString(),
                authorId,
                content,
                privacy,
                null,
                filePaths != null ? filePaths : List.of(),
                List.of(),
                PostCounts.zero(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    public static Post createShare(String authorId, String content,
                                   Privacy privacy, String originalPostId,
                                   Privacy originalPrivacy) {
        if (!originalPrivacy.isVisibleTo(false, false))
            throw new PostDomainException("Cannot share a non-public post");

        return new Post(
                UUID.randomUUID().toString(),
                authorId,
                content,
                privacy,
                originalPostId,
                List.of(),
                List.of(),
                PostCounts.zero(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    public static Post reconstitute(String id, String authorId, String content,
                                    Privacy privacy, String sharedFromPostId,
                                    List<String> attachedFilePaths, List<String> keywords,
                                    PostCounts counts, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, LocalDateTime deletedAt) {
        return new Post(id, authorId, content, privacy, sharedFromPostId,
                attachedFilePaths, keywords, counts, createdAt, updatedAt, deletedAt);
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

    public void updatePrivacy(String requesterId, Privacy newPrivacy) {
        validateAuthor(requesterId);
        this.privacy   = newPrivacy;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete(String requesterId, boolean isAdmin) {
        if (!isAdmin) validateAuthor(requesterId);
        if (isDeleted()) throw new PostDomainException("Post is already deleted");
        this.deletedAt = LocalDateTime.now();
    }

    public void assignKeywords(List<String> keywords) {
        this.keywords = new ArrayList<>(keywords);
    }

    public void onLiked()           { this.counts = counts.incrementLike(); }
    public void onUnliked()         { this.counts = counts.decrementLike(); }
    public void onShared()          { this.counts = counts.incrementShare(); }
    public void onCommentAdded()    { this.counts = counts.incrementComment(); }
    public void onCommentRemoved()  { this.counts = counts.decrementComment(); }

    // ── Queries ───────────────────────────────────────────────

    public boolean isDeleted()   { return deletedAt != null; }
    public boolean isShared()    { return sharedFromPostId != null; }

    public boolean isVisibleTo(String viewerId, boolean isFriend) {
        if (isDeleted()) return false;
        boolean isOwner = authorId.equals(viewerId);
        return privacy.isVisibleTo(isFriend, isOwner);
    }

    private void validateAuthor(String requesterId) {
        if (!authorId.equals(requesterId))
            throw new PostDomainException("Only the author can perform this action");
    }

    // ── Getters ───────────────────────────────────────────────

    public String getId()                        { return id; }
    public String getAuthorId()                  { return authorId; }
    public String getContent()                   { return content; }
    public Privacy getPrivacy()                  { return privacy; }
    public String getSharedFromPostId()          { return sharedFromPostId; }
    public List<String> getAttachedFilePaths()   { return Collections.unmodifiableList(attachedFilePaths); }
    public List<String> getKeywords()            { return Collections.unmodifiableList(keywords); }
    public PostCounts getCounts()                { return counts; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public LocalDateTime getDeletedAt()          { return deletedAt; }
}
