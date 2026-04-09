package com.socialapp.infrastructure.persistence.notification.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import java.time.LocalDateTime;

/**
 * Relationships (managed externally via NotificationNeo4jRepository):
 *   (User)-[:HAS_NOTIFICATION]→(Notification)
 *   (Notification)-[:BY_USER]→(User)
 */
@Node("Notification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NotificationNode {

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
    private boolean isRead; // primitive boolean → builder tạo read() đúng

    @Property("sentAt")
    private LocalDateTime sentAt; // dùng LocalDateTime cho dễ map với Notification
}