package com.socialapp.domain.relationship.entity;

import com.socialapp.domain.relationship.exception.RelationshipDomainException;

import java.time.LocalDateTime;

/**
 * Entity: BlockRelationship
 * Đại diện cho quan hệ chặn (directed: blockerId → blockedId).
 */
public class BlockRelationship {

    private final String blockerId;
    private final String blockedId;
    private final LocalDateTime createdAt;

    private BlockRelationship(String blockerId, String blockedId, LocalDateTime createdAt) {
        if (blockerId.equals(blockedId))
            throw new RelationshipDomainException("Cannot block yourself");
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = createdAt;
    }

    public static BlockRelationship create(String blockerId, String blockedId) {
        return new BlockRelationship(blockerId, blockedId, LocalDateTime.now());
    }

    public static BlockRelationship reconstitute(String blockerId, String blockedId,
                                                 LocalDateTime createdAt) {
        return new BlockRelationship(blockerId, blockedId, createdAt);
    }

    public String getBlockerId()        { return blockerId; }
    public String getBlockedId()        { return blockedId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}