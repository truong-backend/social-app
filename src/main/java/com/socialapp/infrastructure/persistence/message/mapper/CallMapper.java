package com.socialapp.infrastructure.persistence.message.mapper;

import com.socialapp.domain.message.entity.Call;
import com.socialapp.infrastructure.persistence.message.neo4j.node.CallNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CallMapper {

    public Call toDomain(CallNode n, String senderId, String chatId) {
        return Call.reconstitute(
                n.getId(),
                senderId,
                chatId,
                n.getCallId(),
                parse(n.getCallAt()),
                parse(n.getEndAt()),
                Boolean.TRUE.equals(n.getIsAnswered()),
                Boolean.TRUE.equals(n.getIsVideoCall()),
                parse(n.getSentAt())
        );
    }

    public CallNode toNode(Call c) {
        return CallNode.builder()
                .id(c.getId())
                .callId(c.getCallId())
                .isVideoCall(c.isVideoCall())
                .isAnswered(c.isAnswered())
                .isRejected(false)
                .callAt(str(c.getCallAt()))
                .endAt(str(c.getEndAt()))
                .sentAt(str(c.getSentAt()))
                .build();
    }

    private LocalDateTime parse(String s) { return s == null ? null : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)  { return dt == null ? null : dt.toString(); }
}
