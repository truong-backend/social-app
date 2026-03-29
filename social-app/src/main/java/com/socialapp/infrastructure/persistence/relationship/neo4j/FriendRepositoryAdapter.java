package com.socialapp.infrastructure.persistence.relationship.neo4j;

import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.entity.FriendRequest;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.infrastructure.persistence.relationship.neo4j.RelationshipNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// ── FriendRepositoryAdapter ──────────────────────────────────────────────────

@Component
@RequiredArgsConstructor
class FriendRepositoryAdapter implements FriendRepository {

    private final RelationshipNeo4jRepository neo4j;

    @Override public boolean existsFriendship(String a, String b) {
        return neo4j.existsFriendship(a, b);
    }

    @Override public List<FriendRelationship> findFriendsByUserId(String userId) {
        return neo4j.findFriendsByUserId(userId).stream().map(m ->
                FriendRelationship.reconstitute(
                        userId,
                        (String) m.get("friendId"),
                        parseDateTime(m.get("createdAt"))
                )).toList();
    }

    @Override public FriendRelationship save(FriendRelationship rel) {
        neo4j.createFriendship(rel.getUserId(), rel.getFriendId(), rel.getCreatedAt());
        return rel;
    }

    @Override public void delete(String a, String b) {
        neo4j.deleteFriendship(a, b);
    }

    private LocalDateTime parseDateTime(Object val) {
        return val == null ? LocalDateTime.now() : LocalDateTime.parse(val.toString());
    }
}
