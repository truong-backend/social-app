package com.socialapp.infrastructure.persistence.relationship.neo4j;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Raw Cypher queries cho relationship (FRIEND, REQUEST, BLOCK).
 * Dùng Neo4jClient thay vì Neo4jRepository vì relationship
 * giữa 2 node User không dễ dùng Spring Data.
 */
@Repository
public class RelationshipNeo4jRepository {

    private final Neo4jClient client;

    public RelationshipNeo4jRepository(Neo4jClient client) {
        this.client = client;
    }

    // ── FRIEND ────────────────────────────────────────────────

    public boolean existsFriendship(String userIdA, String userIdB) {
        return client.query("""
                MATCH (a:User {id: $a})-[:FRIEND]-(b:User {id: $b})
                RETURN count(*) > 0 AS exists
                """)
                .bind(userIdA).to("a").bind(userIdB).to("b")
                .fetchAs(Boolean.class).one().orElse(false);
    }

    public void createFriendship(String userIdA, String userIdB, LocalDateTime createdAt) {
        client.query("""
                MATCH (a:User {id: $a}), (b:User {id: $b})
                MERGE (a)-[:FRIEND {createdAt: $createdAt}]-(b)
                """)
                .bind(userIdA).to("a").bind(userIdB).to("b")
                .bind(createdAt.toString()).to("createdAt")
                .run();
    }

    public void deleteFriendship(String userIdA, String userIdB) {
        client.query("""
                MATCH (a:User {id: $a})-[r:FRIEND]-(b:User {id: $b})
                DELETE r
                """)
                .bind(userIdA).to("a").bind(userIdB).to("b").run();
    }

    public List<Map<String, Object>> findFriendsByUserId(String userId) {
        return client.query("""
                MATCH (u:User {id: $userId})-[r:FRIEND]-(friend:User)
                RETURN friend.id AS friendId, r.createdAt AS createdAt
                """)
                .bind(userId).to("userId")
                .fetch().all().stream().toList();
    }

    // ── REQUEST ───────────────────────────────────────────────

    public boolean existsRequest(String senderId, String receiverId) {
        return client.query("""
                MATCH (s:User {id: $s})-[:REQUEST]->(r:User {id: $r})
                RETURN count(*) > 0 AS exists
                """)
                .bind(senderId).to("s").bind(receiverId).to("r")
                .fetchAs(Boolean.class).one().orElse(false);
    }

    public void createRequest(String senderId, String receiverId, LocalDateTime sentAt) {
        client.query("""
                MATCH (s:User {id: $s}), (r:User {id: $r})
                MERGE (s)-[:REQUEST {sentAt: $sentAt}]->(r)
                """)
                .bind(senderId).to("s").bind(receiverId).to("r")
                .bind(sentAt.toString()).to("sentAt").run();
    }

    public void deleteRequest(String senderId, String receiverId) {
        client.query("""
                MATCH (s:User {id: $s})-[r:REQUEST]->(rec:User {id: $rec})
                DELETE r
                """)
                .bind(senderId).to("s").bind(receiverId).to("rec").run();
    }

    public List<Map<String, Object>> findSentRequestsByUserId(String senderId) {
        return client.query("""
                MATCH (s:User {id: $userId})-[r:REQUEST]->(recv:User)
                RETURN recv.id AS receiverId, r.sentAt AS sentAt
                """)
                .bind(senderId).to("userId").fetch().all().stream().toList();
    }

    public List<Map<String, Object>> findReceivedRequestsByUserId(String receiverId) {
        return client.query("""
                MATCH (sender:User)-[r:REQUEST]->(recv:User {id: $userId})
                RETURN sender.id AS senderId, r.sentAt AS sentAt
                """)
                .bind(receiverId).to("userId").fetch().all().stream().toList();
    }

    // ── BLOCK ─────────────────────────────────────────────────

    public boolean existsBlock(String blockerId, String blockedId) {
        return client.query("""
                MATCH (b:User {id: $b})-[:BLOCK]->(t:User {id: $t})
                RETURN count(*) > 0 AS exists
                """)
                .bind(blockerId).to("b").bind(blockedId).to("t")
                .fetchAs(Boolean.class).one().orElse(false);
    }

    public void createBlock(String blockerId, String blockedId, LocalDateTime createdAt) {
        client.query("""
                MATCH (b:User {id: $b}), (t:User {id: $t})
                MERGE (b)-[:BLOCK {createdAt: $createdAt}]->(t)
                """)
                .bind(blockerId).to("b").bind(blockedId).to("t")
                .bind(createdAt.toString()).to("createdAt").run();
    }

    public void deleteBlock(String blockerId, String blockedId) {
        client.query("""
                MATCH (b:User {id: $b})-[r:BLOCK]->(t:User {id: $t})
                DELETE r
                """)
                .bind(blockerId).to("b").bind(blockedId).to("t").run();
    }

    public List<Map<String, Object>> findBlockedByUserId(String blockerId) {
        return client.query("""
                MATCH (b:User {id: $userId})-[r:BLOCK]->(t:User)
                RETURN t.id AS blockedId, r.createdAt AS createdAt
                """)
                .bind(blockerId).to("userId").fetch().all().stream().toList();
    }
}
