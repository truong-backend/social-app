package com.socialapp.domain.model.entity;

import com.socialapp.domain.model.valueobject.CommentContent;
import com.socialapp.domain.model.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity: Comment
 * Identity: id
 * Thuộc Aggregate Post.
 *
 * Rules:
 *   - Chỉ đính kèm tối đa 1 file
 *   - Comment có thể reply lồng nhau (REPLIED)
 */
public class Comment {

    private final String         id;
    private final UserId         authorId;
    private final LocalDateTime  createdAt;

    private CommentContent       content;
    private LocalDateTime        updatedAt;
    private int                  likeCount;
    private int                  replyCount;
    private FileEntity           attachedFile;   // tối đa 1 file

    private final List<Comment>  replies = new ArrayList<>();

    public Comment(String id, UserId authorId, CommentContent content) {
        this.id        = id;
        this.authorId  = authorId;
        this.content   = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    // ── Business methods ─────────────────────────────────────

    public void edit(CommentContent newContent) {
        this.content   = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    /** Rule: chỉ được đính kèm 1 file duy nhất */
    public void attachFile(FileEntity file) {
        this.attachedFile = file;
    }

    public void like()   { this.likeCount++; }
    public void unlike() { if (likeCount > 0) this.likeCount--; }

    public void addReply(Comment reply) {
        this.replies.add(reply);
        this.replyCount++;
    }

    // ── Getters ──────────────────────────────────────────────

    public String           getId()           { return id; }
    public UserId           getAuthorId()     { return authorId; }
    public CommentContent   getContent()      { return content; }
    public LocalDateTime    getCreatedAt()    { return createdAt; }
    public LocalDateTime    getUpdatedAt()    { return updatedAt; }
    public int              getLikeCount()    { return likeCount; }
    public int              getReplyCount()   { return replyCount; }
    public FileEntity       getAttachedFile() { return attachedFile; }
    public List<Comment>    getReplies()      { return Collections.unmodifiableList(replies); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Comment)) return false;
        return id.equals(((Comment) o).id);
    }

    @Override public int hashCode() { return id.hashCode(); }


}