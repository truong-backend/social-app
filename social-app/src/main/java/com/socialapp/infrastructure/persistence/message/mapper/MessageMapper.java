package com.socialapp.infrastructure.persistence.message.mapper;

import com.socialapp.domain.message.entity.Message;
import com.socialapp.infrastructure.persistence.message.neo4j.MessageNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MessageMapper {

    public Message toDomain(MessageNode n) {
        return Message.reconstitute(
                n.getId(), n.getSenderId(), n.getChatId(),
                n.getContent(),
                n.getAttachedFilePaths() != null ? n.getAttachedFilePaths() : List.of(),
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
                .senderId(m.getSenderId())
                .chatId(m.getChatId())
                .content(m.getContent())
                .attachedFilePaths(m.getAttachedFilePaths())
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
