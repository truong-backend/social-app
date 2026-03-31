package com.socialapp.infrastructure.persistence.notification.neo4j.repository;

import com.socialapp.infrastructure.persistence.notification.neo4j.node.NotificationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationNeo4jRepository extends Neo4jRepository<NotificationNode, String> {

    // Thông báo của một user, sắp xếp mới nhất
    @Query("""
            MATCH (u:User {id: $ownerId})-[:HAS_NOTIFICATION]->(n:Notification)
            RETURN n ORDER BY n.sentAt DESC
            SKIP $skip LIMIT $limit
            """)
    List<NotificationNode> findByOwnerId(String ownerId, int skip, int limit);

    // Đếm chưa đọc
    @Query("""
            MATCH (u:User {id: $ownerId})-[:HAS_NOTIFICATION]->(n:Notification {isRead: false})
            RETURN count(n)
            """)
    long countUnreadByOwnerId(String ownerId);

    // Lấy byUserId qua relationship BY_USER
    @Query("""
           MATCH (n:Notification {id: $notifId})-[:BY_USER]->(u:User)
           RETURN u.id
           """)
    String findByUserIdByNotifId(String notifId);

    // ── Relationships ────────────────────────────────────────

    // (User)-[:HAS_NOTIFICATION]→(Notification)
    @Query("""
           MATCH (u:User {id: $ownerId}), (n:Notification {id: $notifId})
           MERGE (u)-[:HAS_NOTIFICATION]->(n)
           """)
    void linkOwnerToNotification(String ownerId, String notifId);

    // (Notification)-[:BY_USER]→(User)
    @Query("""
           MATCH (n:Notification {id: $notifId}), (u:User {id: $byUserId})
           MERGE (n)-[:BY_USER]->(u)
           """)
    void linkNotificationByUser(String notifId, String byUserId);
}
