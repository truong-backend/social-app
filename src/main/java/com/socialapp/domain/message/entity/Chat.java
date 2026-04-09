package com.socialapp.domain.message.entity;

import com.socialapp.domain.message.exception.MessageDomainException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity: Chat
 * Aggregate root cho conversation — quản lý thành viên.
 */
public class Chat {

    private final String id;
    private final List<String> memberIds;
    private final LocalDateTime createdAt;

    private Chat(String id, List<String> memberIds, LocalDateTime createdAt) {
        if (memberIds == null || memberIds.size() < 2)
            throw new MessageDomainException("Chat must have at least 2 members");
        this.id        = id;
        this.memberIds = new ArrayList<>(memberIds);
        this.createdAt = createdAt;
    }

    public static Chat createDirect(String userId, String targetId) {
        return new Chat(UUID.randomUUID().toString(),
                List.of(userId, targetId),
                LocalDateTime.now());
    }

    public static Chat reconstitute(String id, List<String> memberIds, LocalDateTime createdAt) {
        return new Chat(id, memberIds, createdAt);
    }

    public boolean hasMember(String userId) {
        return memberIds.contains(userId);
    }

    public void validateMember(String userId) {
        if (!hasMember(userId))
            throw new MessageDomainException("User is not a member of this chat");
    }

    public String getOtherMember(String userId) {
        return memberIds.stream()
                .filter(id -> !id.equals(userId))
                .findFirst()
                .orElseThrow(() -> new com.socialapp.domain.message.exception.MessageDomainException(
                        "No other member found"));
    }

    public String getId()                    { return id; }
    public List<String> getMemberIds()       { return Collections.unmodifiableList(memberIds); }
    public LocalDateTime getCreatedAt()      { return createdAt; }


}
