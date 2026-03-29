package com.socialapp.infrastructure.persistence.message.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Chat")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatNode {

    @Id
    private String id;

    @Property("memberIds")
    private List<String> memberIds;

    @Property("createdAt")
    private String createdAt;
}