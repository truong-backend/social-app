package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node("Message")
public class MessageNode {

    @Id
    private String id;

    @Property("content")
    private String content;

    @Property("sentAt")
    private LocalDateTime sentAt;

    @Property("isRead")
    private boolean isRead;

    @Property("updatedAt")
    private LocalDateTime updatedAt;

    @Property("deletedAt")
    private LocalDateTime deletedAt;

    @Property("callId")
    private String callId;

    @Property("callAt")
    private LocalDateTime callAt;

    @Property("endAt")
    private LocalDateTime endAt;

    @Property("isAnswered")
    private Boolean isAnswered;

    @Property("isEnded")
    private Boolean isEnded;

    @Property("isVideoCall")
    private Boolean isVideoCall;

    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.OUTGOING)
    private FileNode attachedFile;

    // ===== Constructors =====

    public MessageNode() {
    }

    public MessageNode(String id, String content, LocalDateTime sentAt,
                       boolean isRead, LocalDateTime updatedAt,
                       LocalDateTime deletedAt, String callId,
                       LocalDateTime callAt, LocalDateTime endAt,
                       Boolean isAnswered, Boolean isEnded,
                       Boolean isVideoCall) {
        this.id = id;
        this.content = content;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.callId = callId;
        this.callAt = callAt;
        this.endAt = endAt;
        this.isAnswered = isAnswered;
        this.isEnded = isEnded;
        this.isVideoCall = isVideoCall;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public LocalDateTime getCallAt() {
        return callAt;
    }

    public void setCallAt(LocalDateTime callAt) {
        this.callAt = callAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Boolean getIsAnswered() {
        return isAnswered;
    }

    public void setIsAnswered(Boolean isAnswered) {
        this.isAnswered = isAnswered;
    }

    public Boolean getIsEnded() {
        return isEnded;
    }

    public void setIsEnded(Boolean isEnded) {
        this.isEnded = isEnded;
    }

    public Boolean getIsVideoCall() {
        return isVideoCall;
    }

    public void setIsVideoCall(Boolean isVideoCall) {
        this.isVideoCall = isVideoCall;
    }

    public FileNode getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(FileNode attachedFile) {
        this.attachedFile = attachedFile;
    }

    // giữ nguyên method của m
    public boolean isCall() {
        return callId != null || isVideoCall != null;
    }
}