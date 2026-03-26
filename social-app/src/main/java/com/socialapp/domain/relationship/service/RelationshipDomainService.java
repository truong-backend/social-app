package com.socialapp.domain.relationship.service;

import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.entity.FriendRequest;
import com.socialapp.domain.relationship.exception.RelationshipDomainException;

/**
 * Domain Service: RelationshipDomainService
 *
 * Enforce các business rules liên quan đến quan hệ giữa 2 user:
 *  - Không thể gửi request khi đã là bạn
 *  - Không thể gửi request khi đã bị chặn
 *  - Không thể chặn khi chưa remove friend trước
 */
public class RelationshipDomainService {

    // ── Friend Request ─────────────────────────────────────────

    public void validateSendRequest(String senderId, String receiverId,
                                    boolean alreadyFriends,
                                    boolean requestAlreadyExists,
                                    boolean senderIsBlocked,
                                    boolean receiverIsBlocked) {
        if (alreadyFriends)
            throw new RelationshipDomainException("You are already friends");
        if (requestAlreadyExists)
            throw new RelationshipDomainException("Friend request already sent");
        if (senderIsBlocked)
            throw new RelationshipDomainException("You have been blocked by this user");
        if (receiverIsBlocked)
            throw new RelationshipDomainException("You have blocked this user. Unblock first");
    }

    public void validateDeleteRequest(boolean requestExists) {
        if (!requestExists)
            throw new RelationshipDomainException("No pending friend request found");
    }

    public void validateAcceptRequest(boolean requestExists) {
        if (!requestExists)
            throw new RelationshipDomainException("No pending friend request to accept");
    }

    // ── Unfriend ──────────────────────────────────────────────

    public void validateUnfriend(boolean areFriends) {
        if (!areFriends)
            throw new RelationshipDomainException("You are not friends with this user");
    }

    // ── Block ─────────────────────────────────────────────────

    public void validateBlock(boolean alreadyBlocked) {
        if (alreadyBlocked)
            throw new RelationshipDomainException("User is already blocked");
    }

    public void validateUnblock(boolean isBlocked) {
        if (!isBlocked)
            throw new RelationshipDomainException("User is not blocked");
    }

    // ── Factory helpers ───────────────────────────────────────

    public FriendRequest createRequest(String senderId, String receiverId) {
        return FriendRequest.create(senderId, receiverId);
    }

    public FriendRelationship createFriendship(String userIdA, String userIdB) {
        return FriendRelationship.create(userIdA, userIdB);
    }

    public BlockRelationship createBlock(String blockerId, String blockedId) {
        return BlockRelationship.create(blockerId, blockedId);
    }
}