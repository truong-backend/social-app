package com.socialapp.infrastructure.persistence.message.neo4j;

import com.socialapp.domain.message.entity.Call;
import com.socialapp.domain.message.repository.CallRepository;
import com.socialapp.infrastructure.persistence.message.mapper.CallMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.node.CallNode;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.CallNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class CallRepositoryAdapter implements CallRepository {

    private final CallNeo4jRepository neo4j;
    private final CallMapper mapper;

    @Override
    public Optional<Call> findByCallId(String callId) {
        return neo4j.findByCallId(callId).map(node -> {
            String senderId = neo4j.findSenderIdByCallId(node.getId());
            String chatId   = neo4j.findChatIdByCallId(node.getId());
            return mapper.toDomain(node, senderId, chatId);
        });
    }

    @Override
    public Call save(Call call) {
        CallNode saved = neo4j.save(mapper.toNode(call));

        // (Chat)-[:HAS_MESSAGE]→(Call)
        neo4j.linkChatToCall(call.getChatId(), saved.getId());

        // (User)-[:SENT]→(Call)
        neo4j.linkUserSentCall(call.getSenderId(), saved.getId());

        return mapper.toDomain(saved, call.getSenderId(), call.getChatId());
    }
}
