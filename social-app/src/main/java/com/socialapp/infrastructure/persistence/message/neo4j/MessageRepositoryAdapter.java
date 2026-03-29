package com.socialapp.infrastructure.persistence.message.neo4j;

import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.infrastructure.persistence.message.mapper.MessageMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.MessageNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class MessageRepositoryAdapter implements MessageRepository {

    private final MessageNeo4jRepository neo4j;
    private final MessageMapper          mapper;

    @Override public Optional<Message> findById(String id) {
        return neo4j.findById(id).map(mapper::toDomain);
    }

    @Override public List<Message> findByChatId(String chatId, int skip, int limit) {
        return neo4j.findByChatId(chatId, skip, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public Message save(Message message) {
        return mapper.toDomain(neo4j.save(mapper.toNode(message)));
    }

    @Override public void deleteById(String id) {
        neo4j.deleteById(id);
    }
}
