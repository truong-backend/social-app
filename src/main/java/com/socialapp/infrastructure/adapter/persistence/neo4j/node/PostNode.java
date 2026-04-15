package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Post")
public class PostNode {

    @Id
    private String id;

    @Property("content")
    private String content;

    @Property("privacy")
    private String privacy;

    @Property("likeCount")
    private int likeCount;

    @Property("shareCount")
    private int shareCount;

    @Property("commentCount")
    private int commentCount;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Property("deletedAt")
    private LocalDateTime deletedAt;

    @Relationship(type = "ATTACH_FILES", direction = Relationship.Direction.OUTGOING)
    private List<FileNode> attachments = new ArrayList<>();

    @Relationship(type = "HAS_COMMMENT", direction = Relationship.Direction.OUTGOING)
    private List<CommentNode> comments = new ArrayList<>();

    @Relationship(type = "HAS_KEYWORDS", direction = Relationship.Direction.OUTGOING)
    private List<KeywordNode> keywords = new ArrayList<>();

    @Relationship(type = "SHARED", direction = Relationship.Direction.OUTGOING)
    private PostNode sharedFrom;

    // ===== Constructors =====

    public PostNode() {
    }

    public PostNode(String id, String content, String privacy,
                    int likeCount, int shareCount, int commentCount,
                    LocalDateTime createdAt, LocalDateTime updatedAt,
                    LocalDateTime deletedAt) {
        this.id = id;
        this.content = content;
        this.privacy = privacy;
        this.likeCount = likeCount;
        this.shareCount = shareCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public PostNode(String id, String content, String privacy,
                    int likeCount, int shareCount, int commentCount,
                    LocalDateTime createdAt, LocalDateTime updatedAt,
                    LocalDateTime deletedAt,
                    List<FileNode> attachments,
                    List<CommentNode> comments,
                    List<KeywordNode> keywords,
                    PostNode sharedFrom) {
        this.id = id;
        this.content = content;
        this.privacy = privacy;
        this.likeCount = likeCount;
        this.shareCount = shareCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        this.comments = comments != null ? comments : new ArrayList<>();
        this.keywords = keywords != null ? keywords : new ArrayList<>();
        this.sharedFrom = sharedFrom;
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

    public void setContent(String v) {
        this.content = v;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String v) {
        this.privacy = v;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int v) {
        this.likeCount = v;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int v) {
        this.shareCount = v;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int v) {
        this.commentCount = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime v) {
        this.updatedAt = v;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime v) {
        this.deletedAt = v;
    }

    public List<FileNode> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileNode> v) {
        this.attachments = v;
    }

    public List<CommentNode> getComments() {
        return comments;
    }

    public void setComments(List<CommentNode> v) {
        this.comments = v;
    }

    public List<KeywordNode> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeywordNode> v) {
        this.keywords = v;
    }

    public PostNode getSharedFrom() {
        return sharedFrom;
    }

    public void setSharedFrom(PostNode v) {
        this.sharedFrom = v;
    }
}