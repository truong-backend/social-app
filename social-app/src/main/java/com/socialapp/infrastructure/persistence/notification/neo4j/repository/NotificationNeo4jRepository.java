package com.socialapp.infrastructure.persistence.notification.neo4j.repository;

import com.socialapp.infrastructure.persistence.notification.neo4j.node.NotificationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationNeo4jRepository extends Neo4jRepository<NotificationNode, String> {

    @Query("""
            MATCH (n:Notification {ownerId: $ownerId})
            RETURN n ORDER BY n.sentAt DESC
            SKIP $skip LIMIT $limit
            """)
    List<NotificationNode> findByOwnerId(String ownerId, int skip, int limit);

    @Query("""
            MATCH (n:Notification {ownerId: $ownerId, isRead: false})
            RETURN count(n)
            """)
    long countUnreadByOwnerId(String ownerId);
}
