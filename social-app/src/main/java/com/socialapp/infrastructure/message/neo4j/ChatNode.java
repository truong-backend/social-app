package com.socialapp.infrastructure.message.neo4j;

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