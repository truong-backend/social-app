package com.socialapp.domain.relationship.entity;

import com.socialapp.domain.relationship.exception.RelationshipDomainException;

import java.time.LocalDateTime;

/**
 * Entity: FriendRelationship
 * Đại diện cho mối quan hệ bạn bè giữa 2 user.
 * Là undirected (userId + friendId được sắp xếp để tránh duplicate).
 */
public class FriendRelationship {

    private final String userId;
    private final String friendId;
    private final LocalDateTime createdAt;

    private FriendRelationship(String userId, String friendId, LocalDateTime createdAt) {
        this.userId    = userId;
        this.friendId  = friendId;
        this.createdAt = createdAt;
    }

    public static FriendRelationship create(String userIdA, String userIdB) {
        // Sắp xếp để đảm bảo không có duplicate (A-B và B-A)
        String lower  = userIdA.compareTo(userIdB) <= 0 ? userIdA : userIdB;
        String higher = userIdA.compareTo(userIdB) <= 0 ? userIdB : userIdA;
        return new FriendRelationship(lower, higher, LocalDateTime.now());
    }

    public static FriendRelationship reconstitute(String userId, String friendId,
                                                  LocalDateTime createdAt) {
        return new FriendRelationship(userId, friendId, createdAt);
    }

    public boolean involves(String userId) {
        return this.userId.equals(userId) || this.friendId.equals(userId);
    }

    public String getOtherUserId(String userId) {
        if (this.userId.equals(userId)) return friendId;
        if (this.friendId.equals(userId)) return userId;
        throw new RelationshipDomainException("User not part of this friendship");
    }

    public String getUserId()           { return userId; }
    public String getFriendId()         { return friendId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}