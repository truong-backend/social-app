package com.socialapp.infrastructure.persistence.notification.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Notification")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
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
    private Boolean isRead;

    @Property("sentAt")
    private String sentAt;
}

