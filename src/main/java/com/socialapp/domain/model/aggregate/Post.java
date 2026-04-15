package com.socialapp.domain.model.aggregate;

import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Keyword;
import com.socialapp.domain.model.valueobject.PostContent;
import com.socialapp.domain.model.valueobject.PostPrivacy;
import com.socialapp.domain.model.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: Post
 * ─────────────────────────────────────────────────────────────
 * Quản lý bài viết + bình luận + file đính kèm + từ khóa.
 *
 * Rules:
 *   - Nội dung ≤ 10.000 ký tự (enforce trong PostContent VO)
 *   - Tối đa 10 file đính kèm
 *   - Chỉ bài PUBLIC mới chia sẻ được
 * ─────────────────────────────────────────────────────────────
 * Quan hệ trong graph:
 *   Post --HAS_COMMENT--> Comment
 *   Post --ATTACH_FILES--> File
 *   Post --HAS_KEYWORDS--> Keyword
 *   User --POSTED--> Post
 *   User --LIKED--> Post
 *   Post --SHARED--> Post (self-loop)
 */
public class Post {

    private static final int MAX_ATTACHMENTS = 10;

    // ── Identity ─────────────────────────────────────────────
    private final String id;

    // ── Value Objects ─────────────────────────────────────────
    private final UserId       authorId;
    private       PostContent  content;
    private       PostPrivacy  privacy;

    // ── Counters ─────────────────────────────────────────────
    private int likeCount;
    private int shareCount;
    private int commentCount;

    // ── Timestamps ────────────────────────────────────────────
    private final LocalDateTime createdAt;
    private       LocalDateTime updatedAt;
    private       LocalDateTime deletedAt;

    // ── Child entities ────────────────────────────────────────
    private final List<FileEntity> attachments = new ArrayList<>();
    private final List<Comment>    comments    = new ArrayList<>();
    private final List<Keyword>    keywords    = new ArrayList<>();

    // ── Optional: shared from ─────────────────────────────────
    private String sharedFromPostId;

    // ── Constructors ─────────────────────────────────────────

    public Post(String id, UserId authorId, PostContent content, PostPrivacy privacy) {
        this.id        = id;
        this.authorId  = authorId;
        this.content   = content;
        this.privacy   = privacy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ── Business Methods ─────────────────────────────────────

    public void editContent(PostContent newContent) {
        if (deletedAt != null)
            throw new IllegalStateException("Cannot edit a deleted post");
        this.content   = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePrivacy(PostPrivacy privacy) {
        this.privacy   = privacy;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() { return deletedAt != null; }

    // ── Reactions ─────────────────────────────────────────────

    public void like()   { likeCount++; }
    public void unlike() { if (likeCount > 0) likeCount--; }

    /** Rule: chỉ share bài PUBLIC */
    public void incrementShareCount() {
        if (privacy != PostPrivacy.PUBLIC)
            throw new IllegalStateException("Only PUBLIC posts can be shared");
        shareCount++;
    }

    // ── Attachments ───────────────────────────────────────────

    public void attachFile(FileEntity file) {
        if (attachments.size() >= MAX_ATTACHMENTS)
            throw new IllegalStateException("Post can have at most " + MAX_ATTACHMENTS + " attachments");
        attachments.add(file);
    }

    public List<FileEntity> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    // ── Comments ──────────────────────────────────────────────

    public void addComment(Comment comment) {
        comments.add(comment);
        commentCount++;
    }

    public void removeComment(String commentId) {
        boolean removed = comments.removeIf(c -> c.getId().equals(commentId));
        if (removed && commentCount > 0) commentCount--;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    // ── Keywords ──────────────────────────────────────────────

    public void setKeywords(List<Keyword> keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

    public List<Keyword> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    // ── Getters ──────────────────────────────────────────────

    public String        getId()               { return id; }
    public UserId        getAuthorId()          { return authorId; }
    public PostContent   getContent()           { return content; }
    public PostPrivacy   getPrivacy()           { return privacy; }
    public int           getLikeCount()         { return likeCount; }
    public int           getShareCount()        { return shareCount; }
    public int           getCommentCount()      { return commentCount; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public LocalDateTime getDeletedAt()         { return deletedAt; }
    public String        getSharedFromPostId()  { return sharedFromPostId; }

    public void setSharedFromPostId(String id)  { this.sharedFromPostId = id; }
    public void setLikeCount(int v)             { this.likeCount = v; }
    public void setShareCount(int v)            { this.shareCount = v; }
    public void setCommentCount(int v)          { this.commentCount = v; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Post)) return false;
        return id.equals(((Post) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}