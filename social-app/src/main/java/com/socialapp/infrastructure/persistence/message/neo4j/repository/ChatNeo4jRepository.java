package com.socialapp.infrastructure.persistence.message.neo4j.repository;

import com.socialapp.infrastructure.persistence.message.neo4j.node.ChatNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatNeo4jRepository extends Neo4jRepository<ChatNode, String> {

    @Query("""
           MATCH (c:Chat)
           WHERE $userIdA IN c.memberIds AND $userIdB IN c.memberIds
             AND size(c.memberIds) = 2
           RETURN c LIMIT 1
           """)
    Optional<ChatNode> findDirectChatBetween(String userIdA, String userIdB);

    @Query("MATCH (c:Chat) WHERE $userId IN c.memberIds RETURN c ORDER BY c.createdAt DESC")
    List<ChatNode> findByUserId(String userId);

    @Query("""
           MATCH (c:Chat)
           WHERE $userId IN c.memberIds
             AND ANY(mid IN c.memberIds WHERE mid <> $userId
                 AND EXISTS {
                       MATCH (u:User {id: mid})
                       WHERE u.username CONTAINS $query
                          OR u.familyName CONTAINS $query
                          OR u.givenName  CONTAINS $query
                     })
           RETURN c
           """)
    List<ChatNode> searchByUserId(String query, String userId);
}
