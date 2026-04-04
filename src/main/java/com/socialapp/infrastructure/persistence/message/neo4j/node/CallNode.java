package com.socialapp.infrastructure.persistence.message.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Neo4j node for Call (subclass of Message).
 * Relationships (managed externally):
 *   (Chat)-[:HAS_MESSAGE]→(Call)
 *   (User)-[:SENT]→(Call)
 *
 * Lưu ý: Call kế thừa Message, nhưng trong Neo4j ta dùng label riêng "Call".
 */
@Node("Call")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CallNode {

    @Id
    private String id;

    /** callId dùng cho ZegoCloud roomID */
    @Property("callId")
    private String callId;

    @Property("isVideoCall")
    private Boolean isVideoCall;

    @Property("isAnswered")
    private Boolean isAnswered;

    @Property("isRejected")
    private Boolean isRejected;

    @Property("callAt")
    private String callAt;

    @Property("endAt")
    private String endAt;

    @Property("sentAt")
    private String sentAt;
}
