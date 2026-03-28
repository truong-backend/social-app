package com.socialapp.infrastructure.message.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageNeo4jRepository extends Neo4jRepository<MessageNode, String> {

    @Query("""
           MATCH (c:Chat {id: $chatId})-[:HAS_MESSAGE]->(m:Message)
           RETURN m ORDER BY m.sentAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<MessageNode> findByChatId(String chatId, int skip, int limit);
}
