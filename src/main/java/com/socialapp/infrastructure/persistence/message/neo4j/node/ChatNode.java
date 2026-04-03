package com.socialapp.infrastructure.persistence.message.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via ChatNeo4jRepository):
 *   (User)-[:IS_MEMBER_OF]→(Chat)
 *   (Chat)-[:HAS_MESSAGE]→(Message)
 */
@Node("Chat")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatNode {

    @Id
    private String id;

    @Property("createdAt")
    private String createdAt;
}
