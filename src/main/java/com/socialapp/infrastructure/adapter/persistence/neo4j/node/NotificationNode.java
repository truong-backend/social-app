package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node("Notification")
public class NotificationNode {

    @Id
    private String id;

    @Property("action")
    private String action;

    @Property("isRead")
    private boolean isRead;

    @Property("targetType")
    private String targetType;

    @Property("targetId")
    private String targetId;

    @Property("sentAt")
    private LocalDateTime sentAt;

    @Relationship(type = "BY_USER", direction = Relationship.Direction.OUTGOING)
    private UserNode byUser;

    // ===== Constructors =====

    public NotificationNode() {
    }

    public NotificationNode(String id, String action, boolean isRead,
                            String targetType, String targetId,
                            LocalDateTime sentAt, UserNode byUser) {
        this.id = id;
        this.action = action;
        this.isRead = isRead;
        this.targetType = targetType;
        this.targetId = targetId;
        this.sentAt = sentAt;
        this.byUser = byUser;
    }

    // ===== Getters / Setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public UserNode getByUser() {
        return byUser;
    }

    public void setByUser(UserNode byUser) {
        this.byUser = byUser;
    }
}