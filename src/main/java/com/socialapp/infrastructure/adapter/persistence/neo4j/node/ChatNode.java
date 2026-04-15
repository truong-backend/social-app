package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Chat")
public class ChatNode {

    @Id
    private String id;

    @Property("createdAt")
    private LocalDateTime createdAt;

    @Relationship(type = "HAS_MESSAGE", direction = Relationship.Direction.OUTGOING)
    private List<MessageNode> messages = new ArrayList<>();

    public ChatNode() {
    }

    public ChatNode(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MessageNode> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageNode> messages) {
        this.messages = messages;
    }
}