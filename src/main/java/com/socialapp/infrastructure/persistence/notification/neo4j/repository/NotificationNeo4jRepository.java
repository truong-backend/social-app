package com.socialapp.infrastructure.persistence.notification.neo4j.repository;

import com.socialapp.infrastructure.persistence.notification.neo4j.node.NotificationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationNeo4jRepository extends Neo4jRepository<NotificationNode, String> {

    @Query("MATCH (n:NotificationNode {ownerId: $ownerId}) " +
            "RETURN n ORDER BY n.sentAt DESC SKIP $skip LIMIT $limit")
    List<NotificationNode> findByOwnerIdOrderBySentAtDesc(
            @Param("ownerId") String ownerId,
            @Param("skip") int skip,
            @Param("limit") int limit);

    @Query("MATCH (n:NotificationNode {id: $id}) SET n.isRead = true")
    void markAsRead(@Param("id") String id);

    @Query("MATCH (n:NotificationNode {ownerId: $ownerId, isRead: false}) SET n.isRead = true")
    void markAllAsRead(@Param("ownerId") String ownerId);

    @Query("MATCH (n:NotificationNode {ownerId: $ownerId, isRead: false}) RETURN count(n)")
    long countUnreadByOwnerId(@Param("ownerId") String ownerId);
}