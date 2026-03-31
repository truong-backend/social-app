package com.socialapp.infrastructure.persistence.message.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via MessageNeo4jRepository):
 *   (Chat)-[:HAS_MESSAGE]→(Message)
 *   (User)-[:SENT]→(Message)
 *   (Message)-[:ATTACH_FILE]→(File)
 *
 * Giữ deletedForEveryoneAt + deletedForSenderAt (chi tiết hơn tài liệu, logic cần thiết).
 */
@Node("Message")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MessageNode {

    @Id
    private String id;

    @Property("content")
    private String content;

    @Property("isRead")
    private Boolean isRead;

    @Property("deletedForEveryoneAt")
    private String deletedForEveryoneAt;

    @Property("deletedForSenderAt")
    private String deletedForSenderAt;

    @Property("sentAt")
    private String sentAt;

    @Property("updatedAt")
    private String updatedAt;
}
