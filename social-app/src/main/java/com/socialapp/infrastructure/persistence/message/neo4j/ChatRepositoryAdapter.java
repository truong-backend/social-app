package com.socialapp.infrastructure.persistence.message.neo4j;

import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.infrastructure.persistence.message.mapper.ChatMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.node.ChatNode;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.ChatNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ChatRepositoryAdapter implements ChatRepository {

    private final ChatNeo4jRepository neo4j;
    private final ChatMapper          mapper;

    @Override
    public Optional<Chat> findById(String id) {
        return neo4j.findById(id).map(node -> {
            List<String> memberIds = neo4j.findMemberIdsByChatId(node.getId());
            return mapper.toDomain(node, memberIds);
        });
    }

    @Override
    public Optional<Chat> findDirectChatBetween(String a, String b) {
        return neo4j.findDirectChatBetween(a, b).map(node -> {
            List<String> memberIds = neo4j.findMemberIdsByChatId(node.getId());
            return mapper.toDomain(node, memberIds);
        });
    }

    @Override
    public List<Chat> findByUserId(String userId) {
        return neo4j.findByUserId(userId).stream()
                .map(node -> {
                    List<String> memberIds = neo4j.findMemberIdsByChatId(node.getId());
                    return mapper.toDomain(node, memberIds);
                })
                .toList();
    }

    @Override
    public List<Chat> searchByUserId(String query, String userId) {
        return neo4j.searchByUserId(query, userId).stream()
                .map(node -> {
                    List<String> memberIds = neo4j.findMemberIdsByChatId(node.getId());
                    return mapper.toDomain(node, memberIds);
                })
                .toList();
    }

    @Override
    public Chat save(Chat chat) {
        ChatNode saved = neo4j.save(mapper.toNode(chat));

        // (User)-[:IS_MEMBER_OF]→(Chat) — tạo cho tất cả members
        chat.getMemberIds()
                .forEach(memberId -> neo4j.linkUserToChat(memberId, saved.getId()));

        return mapper.toDomain(saved, chat.getMemberIds());
    }
}
