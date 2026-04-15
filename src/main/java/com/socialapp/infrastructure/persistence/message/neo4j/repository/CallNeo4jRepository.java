package com.socialapp.infrastructure.persistence.message.neo4j.repository;

import com.socialapp.infrastructure.persistence.message.neo4j.node.CallNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallNeo4jRepository extends Neo4jRepository<CallNode, String> {

//    Optional<CallNode> findByCallId(String callId);

    // Lấy senderId qua (User)-[:SENT]→(Call)
    @Query("""
           MATCH (u:User)-[:SENT]->(c:Call {id: $callId})
           RETURN u.id
           """)
    String findSenderIdByCallId(String callId);

    // Lấy chatId qua (Chat)-[:HAS_MESSAGE]→(Call)
    @Query("""
           MATCH (ch:Chat)-[:HAS_MESSAGE]->(c:Call {id: $callId})
           RETURN ch.id
           """)
    String findChatIdByCallId(String callId);

    // (Chat)-[:HAS_MESSAGE]→(Call)
    @Query("""
           MATCH (ch:Chat {id: $chatId}), (c:Call {id: $callId})
           MERGE (ch)-[:HAS_MESSAGE]->(c)
           """)
    void linkChatToCall(String chatId, String callId);

    // (User)-[:SENT]→(Call)
    @Query("""
           MATCH (u:User {id: $senderId}), (c:Call {id: $callId})
           MERGE (u)-[:SENT]->(c)
           """)
    void linkUserSentCall(String senderId, String callId);

    @Query("MATCH (c:CallNode {callId: $callId}) RETURN c")
    Optional<CallNode> findByCallId(@Param("callId") String callId);

    @Query("MATCH (c:CallNode {chatId: $chatId}) WHERE c.endAt IS NULL RETURN c LIMIT 1")
    Optional<CallNode> findActiveCallByChatId(@Param("chatId") String chatId);
}
