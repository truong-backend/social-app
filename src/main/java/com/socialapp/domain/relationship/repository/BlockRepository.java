package com.socialapp.domain.relationship.repository;

import com.socialapp.domain.relationship.entity.BlockRelationship;

import java.util.List;

public interface BlockRepository {

    boolean exists(String blockerId, String blockedId);

    List<BlockRelationship> findBlockedByUserId(String blockerId);

    BlockRelationship save(BlockRelationship block);

    void delete(String blockerId, String blockedId);
}