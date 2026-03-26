package com.socialapp.domain.relationship.entity;

import com.socialapp.domain.relationship.exception.RelationshipDomainException;

import java.time.LocalDateTime;

/**
 * Entity: FriendRequest
 * Đại diện cho lời mời kết bạn đang chờ xử lý.
 */
public class FriendRequest {

    private final String senderId;
    private final String receiverId;
    private final LocalDateTime sentAt;

    private FriendRequest(String senderId, String receiverId, LocalDateTime sentAt) {
        if (senderId.equals(receiverId))
            throw new RelationshipDomainException("Cannot send friend request to yourself");
        this.senderId   = senderId;
        this.receiverId = receiverId;
        this.sentAt     = sentAt;
    }

    public static FriendRequest create(String senderId, String receiverId) {
        return new FriendRequest(senderId, receiverId, LocalDateTime.now());
    }

    public static FriendRequest reconstitute(String senderId, String receiverId,
                                             LocalDateTime sentAt) {
        return new FriendRequest(senderId, receiverId, sentAt);
    }

    public String getSenderId()         { return senderId; }
    public String getReceiverId()       { return receiverId; }
    public LocalDateTime getSentAt()    { return sentAt; }
}
