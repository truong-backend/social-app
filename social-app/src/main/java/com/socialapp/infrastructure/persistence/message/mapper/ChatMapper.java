package com.socialapp.infrastructure.persistence.message.mapper;

import com.socialapp.domain.message.entity.Chat;
import com.socialapp.infrastructure.persistence.message.neo4j.node.ChatNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ChatMapper {

    public Chat toDomain(ChatNode n) {
        return Chat.reconstitute(
                n.getId(),
                n.getMemberIds() != null ? n.getMemberIds() : List.of(),
                parse(n.getCreatedAt())
        );
    }

    public ChatNode toNode(Chat c) {
        return ChatNode.builder()
                .id(c.getId())
                .memberIds(c.getMemberIds())
                .createdAt(str(c.getCreatedAt()))
                .build();
    }

    private LocalDateTime parse(String s) { return s == null ? LocalDateTime.now() : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)  { return dt == null ? null : dt.toString(); }
}
