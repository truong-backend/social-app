package com.socialapp.infrastructure.persistence.message.mapper;

import com.socialapp.domain.message.entity.Chat;
import com.socialapp.infrastructure.persistence.message.neo4j.node.ChatNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ChatNode không còn lưu memberIds.
 * memberIds được quản lý hoàn toàn qua relationship (User)-[:IS_MEMBER_OF]→(Chat).
 * Khi toDomain(), memberIds được truyền vào từ ChatRepositoryAdapter.
 */
@Component
public class ChatMapper {

    /**
     * Map đầy đủ khi adapter đã resolve memberIds từ graph.
     */
    public Chat toDomain(ChatNode n, List<String> memberIds) {
        return Chat.reconstitute(
                n.getId(),
                memberIds != null ? memberIds : List.of(),
                parse(n.getCreatedAt())
        );
    }

    public ChatNode toNode(Chat c) {
        return ChatNode.builder()
                .id(c.getId())
                .createdAt(str(c.getCreatedAt()))
                .build();
    }

    private LocalDateTime parse(String s) { return s == null ? LocalDateTime.now() : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)  { return dt == null ? null : dt.toString(); }
}
