package com.socialapp.infrastructure.notification.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Node("Notification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
class NotificationNode {

    @Id
    private String id;

    @Property("ownerId")
    private String ownerId;

    @Property("byUserId")
    private String byUserId;

    @Property("action")
    private String action;

    @Property("targetType")
    private String targetType;

    @Property("targetId")
    private String targetId;

    @Property("isRead")
    private Boolean isRead;

    @Property("sentAt")
    private String sentAt;
}

@Repository
interface NotificationNeo4jRepository extends Neo4jRepository<NotificationNode, String> {

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