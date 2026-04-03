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


// ── FriendRequestRepositoryAdapter ──────────────────────────────────────────

@Component
@RequiredArgsConstructor
class FriendRequestRepositoryAdapter implements FriendRequestRepository {

    private final RelationshipNeo4jRepository neo4j;

    @Override public boolean exists(String senderId, String receiverId) {
        return neo4j.existsRequest(senderId, receiverId);
    }

    @Override public Optional<FriendRequest> find(String senderId, String receiverId) {
        if (!neo4j.existsRequest(senderId, receiverId)) return Optional.empty();
        return Optional.of(FriendRequest.reconstitute(senderId, receiverId, LocalDateTime.now()));
    }

    @Override public List<FriendRequest> findSentByUserId(String senderId) {
        return neo4j.findSentRequestsByUserId(senderId).stream().map(m ->
                FriendRequest.reconstitute(senderId, (String) m.get("receiverId"),
                        parseDateTime(m.get("sentAt")))).toList();
    }

    @Override public List<FriendRequest> findReceivedByUserId(String receiverId) {
        return neo4j.findReceivedRequestsByUserId(receiverId).stream().map(m ->
                FriendRequest.reconstitute((String) m.get("senderId"), receiverId,
                        parseDateTime(m.get("sentAt")))).toList();
    }

    @Override public FriendRequest save(FriendRequest req) {
        neo4j.createRequest(req.getSenderId(), req.getReceiverId(), req.getSentAt());
        return req;
    }

    @Override public void delete(String senderId, String receiverId) {
        neo4j.deleteRequest(senderId, receiverId);
    }

    private LocalDateTime parseDateTime(Object val) {
        return val == null ? LocalDateTime.now() : LocalDateTime.parse(val.toString());
    }
}
