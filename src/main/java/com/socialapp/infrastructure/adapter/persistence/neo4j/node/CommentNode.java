package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Comment")
public class CommentNode {

    @Id
    private String id;

    @Property("content")
    private String content;

    @Property("likeCount")
    private int likeCount;

    @Property("replyCount")
    private int replyCount;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.OUTGOING)
    private FileNode attachedFile;

    @Relationship(type = "REPLIED", direction = Relationship.Direction.OUTGOING)
    private List<CommentNode> replies = new ArrayList<>();

    // ===== Constructors =====

    public CommentNode() {
    }

    public CommentNode(String id, String content, int likeCount, int replyCount,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public CommentNode(String id, String content, int likeCount, int replyCount,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       FileNode attachedFile,
                       List<CommentNode> replies) {
        this.id = id;
        this.content = content;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.attachedFile = attachedFile;
        this.replies = replies != null ? replies : new ArrayList<>();
    }

    // ===== Getters / Setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public FileNode getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(FileNode attachedFile) {
        this.attachedFile = attachedFile;
    }

    public List<CommentNode> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentNode> replies) {
        this.replies = replies != null ? replies : new ArrayList<>();
    }
}