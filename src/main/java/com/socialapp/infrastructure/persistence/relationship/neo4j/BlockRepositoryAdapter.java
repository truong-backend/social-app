package com.socialapp.infrastructure.persistence.relationship.neo4j;

import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
class BlockRepositoryAdapter implements BlockRepository {

    private final RelationshipNeo4jRepository neo4j;

    @Override
    public boolean exists(String blockerId, String blockedId) {
        return neo4j.existsBlock(blockerId, blockedId);
    }

    @Override
    public List<BlockRelationship> findBlockedByUserId(String blockerId) {
        return neo4j.findBlockedByUserId(blockerId).stream()
                .map(m -> BlockRelationship.reconstitute(
                        blockerId,
                        (String) m.get("blockedId"),
                        parseDateTime(m.get("createdAt"))))
                .toList();
    }

    @Override
    public List<BlockRelationship> findBlockersByUserId(String userId) {
        return neo4j.findBlockersByUserId(userId).stream()
                .map(m -> BlockRelationship.reconstitute(
                        (String) m.get("blockerId"),
                        userId,
                        parseDateTime(m.get("createdAt"))))
                .toList();
    }

    @Override
    public BlockRelationship save(BlockRelationship block) {
        neo4j.createBlock(block.getBlockerId(), block.getBlockedId(), block.getCreatedAt());
        return block;
    }

    @Override
    public void delete(String blockerId, String blockedId) {
        neo4j.deleteBlock(blockerId, blockedId);
    }

    private LocalDateTime parseDateTime(Object val) {
        return val == null ? LocalDateTime.now() : LocalDateTime.parse(val.toString());
    }
}