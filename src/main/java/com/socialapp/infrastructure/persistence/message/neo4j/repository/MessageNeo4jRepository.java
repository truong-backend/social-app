package com.socialapp.infrastructure.persistence.message.neo4j.repository;

import com.socialapp.infrastructure.persistence.message.neo4j.node.MessageNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageNeo4jRepository extends Neo4jRepository<MessageNode, String> {

    // Tin nhắn của chat, sắp xếp mới nhất
    @Query("""
           MATCH (c:Chat {id: $chatId})-[:HAS_MESSAGE]->(m:Message)
           RETURN m ORDER BY m.sentAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<MessageNode> findByChatId(String chatId, int skip, int limit);

    // ── Context resolvers ────────────────────────────────────

    // Lấy senderId qua (User)-[:SENT]→(Message)
    @Query("""
           MATCH (u:User)-[:SENT]->(m:Message {id: $messageId})
           RETURN u.id
           """)
    String findSenderIdByMessageId(String messageId);

    // Lấy chatId qua (Chat)-[:HAS_MESSAGE]→(Message)
    @Query("""
           MATCH (c:Chat)-[:HAS_MESSAGE]->(m:Message {id: $messageId})
           RETURN c.id
           """)
    String findChatIdByMessageId(String messageId);

    // Lấy danh sách file đính kèm qua (Message)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (m:Message {id: $messageId})-[:ATTACH_FILE]->(f:File)
           RETURN f.path
           """)
    List<String> findAttachedFilePathsByMessageId(String messageId);

    // ── Relationships ────────────────────────────────────────

    // (Chat)-[:HAS_MESSAGE]→(Message)
    @Query("""
           MATCH (c:Chat {id: $chatId}), (m:Message {id: $messageId})
           MERGE (c)-[:HAS_MESSAGE]->(m)
           """)
    void linkChatToMessage(String chatId, String messageId);

    // (User)-[:SENT]→(Message)
    @Query("""
           MATCH (u:User {id: $senderId}), (m:Message {id: $messageId})
           MERGE (u)-[:SENT]->(m)
           """)
    void linkUserSentMessage(String senderId, String messageId);

    // (Message)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (m:Message {id: $messageId}), (f:File {path: $filePath})
           MERGE (m)-[:ATTACH_FILE]->(f)
           """)
    void linkMessageAttachFile(String messageId, String filePath);
}
