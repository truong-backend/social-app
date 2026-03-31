package com.socialapp.infrastructure.persistence.message.neo4j.repository;

import com.socialapp.infrastructure.persistence.message.neo4j.node.ChatNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatNeo4jRepository extends Neo4jRepository<ChatNode, String> {

    // Tìm direct chat giữa 2 user: cả 2 đều IS_MEMBER_OF, và chat chỉ có đúng 2 member
    @Query("""
           MATCH (a:User {id: $userIdA})-[:IS_MEMBER_OF]->(c:Chat)<-[:IS_MEMBER_OF]-(b:User {id: $userIdB})
           WHERE size([(u:User)-[:IS_MEMBER_OF]->(c) | u]) = 2
           RETURN c LIMIT 1
           """)
    Optional<ChatNode> findDirectChatBetween(String userIdA, String userIdB);

    // Tất cả chat của một user, sắp xếp mới nhất
    @Query("""
           MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(c:Chat)
           RETURN c ORDER BY c.createdAt DESC
           """)
    List<ChatNode> findByUserId(String userId);

    // Tìm chat theo tên member khác (không phải mình)
    @Query("""
           MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(c:Chat)<-[:IS_MEMBER_OF]-(other:User)
           WHERE other.id <> $userId
             AND (other.username CONTAINS $query
                  OR other.familyName CONTAINS $query
                  OR other.givenName  CONTAINS $query)
           RETURN DISTINCT c
           """)
    List<ChatNode> searchByUserId(String query, String userId);

    // Lấy danh sách memberIds qua IS_MEMBER_OF
    @Query("""
           MATCH (u:User)-[:IS_MEMBER_OF]->(c:Chat {id: $chatId})
           RETURN u.id
           """)
    List<String> findMemberIdsByChatId(String chatId);

    // ── Relationships ────────────────────────────────────────

    // (User)-[:IS_MEMBER_OF]→(Chat)
    @Query("""
           MATCH (u:User {id: $userId}), (c:Chat {id: $chatId})
           MERGE (u)-[:IS_MEMBER_OF]->(c)
           """)
    void linkUserToChat(String userId, String chatId);
}
