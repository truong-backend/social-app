package com.socialapp.infrastructure.persistence.message.mapper;

import com.socialapp.domain.message.entity.Message;
import com.socialapp.infrastructure.persistence.message.neo4j.node.MessageNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MessageNode không còn lưu: senderId, chatId, attachedFilePaths.
 * Các giá trị đó được quản lý hoàn toàn qua relationship:
 *   (User)-[:SENT]→(Message)
 *   (Chat)-[:HAS_MESSAGE]→(Message)
 *   (Message)-[:ATTACH_FILE]→(File)
 *
 * Khi toDomain(), các field này được truyền vào từ MessageRepositoryAdapter.
 */
@Component
public class MessageMapper {

    /**
     * Map đầy đủ khi adapter đã resolve senderId, chatId, attachedFilePaths từ graph.
     */
    public Message toDomain(MessageNode n, String senderId, String chatId,
                            List<String> attachedFilePaths) {
        return Message.reconstitute(
                n.getId(),
                senderId,
                chatId,
                n.getContent(),
                attachedFilePaths != null ? attachedFilePaths : List.of(),
                Boolean.TRUE.equals(n.getIsRead()),
                parse(n.getDeletedForEveryoneAt()),
                parse(n.getDeletedForSenderAt()),
                parse(n.getSentAt()),
                parse(n.getUpdatedAt())
        );
    }

    public MessageNode toNode(Message m) {
        return MessageNode.builder()
                .id(m.getId())
                .content(m.getContent())
                .isRead(m.isRead())
                .deletedForEveryoneAt(str(m.getDeletedForEveryoneAt()))
                .deletedForSenderAt(str(m.getDeletedForSenderAt()))
                .sentAt(str(m.getSentAt()))
                .updatedAt(str(m.getUpdatedAt()))
                .build();
    }

    private LocalDateTime parse(String s) { return s == null ? null : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)  { return dt == null ? null : dt.toString(); }
}
