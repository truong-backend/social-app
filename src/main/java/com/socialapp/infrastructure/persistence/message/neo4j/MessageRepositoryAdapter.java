package com.socialapp.infrastructure.persistence.message.neo4j;

import com.socialapp.domain.message.entity.Message;
import com.socialapp.domain.message.repository.MessageRepository;
import com.socialapp.infrastructure.persistence.message.mapper.MessageMapper;
import com.socialapp.infrastructure.persistence.message.neo4j.node.MessageNode;
import com.socialapp.infrastructure.persistence.message.neo4j.repository.MessageNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageNeo4jRepository neo4j;
    private final MessageMapper          mapper;

    @Override
    public Optional<Message> findById(String id) {
        return neo4j.findById(id).map(node -> {
            String senderId           = neo4j.findSenderIdByMessageId(node.getId());
            String chatId             = neo4j.findChatIdByMessageId(node.getId());
            List<String> filePaths    = neo4j.findAttachedFilePathsByMessageId(node.getId());
            return mapper.toDomain(node, senderId, chatId, filePaths);
        });
    }

    @Override
    public List<Message> findByChatId(String chatId, int skip, int limit) {
        return neo4j.findByChatId(chatId, skip, limit)
                .stream()
                .map(node -> {
                    String senderId        = neo4j.findSenderIdByMessageId(node.getId());
                    List<String> filePaths = neo4j.findAttachedFilePathsByMessageId(node.getId());
                    return mapper.toDomain(node, senderId, chatId, filePaths);
                })
                .toList();
    }

    @Override
    public Message save(Message message) {
        MessageNode saved = neo4j.save(mapper.toNode(message));
        String messageId = saved.getId();

        // (Chat)-[:HAS_MESSAGE]→(Message)
        neo4j.linkChatToMessage(message.getChatId(), messageId);

        // (User)-[:SENT]→(Message)
        neo4j.linkUserSentMessage(message.getSenderId(), messageId);

        // (Message)-[:ATTACH_FILE]→(File)
        if (message.getAttachedFilePaths() != null) {
            message.getAttachedFilePaths()
                    .forEach(path -> neo4j.linkMessageAttachFile(messageId, path));
        }

        return mapper.toDomain(saved,
                message.getSenderId(),
                message.getChatId(),
                message.getAttachedFilePaths());
    }

    @Override
    public void deleteById(String id) {
        neo4j.deleteById(id);
    }

//    @Override
//    public Message save(Message message) {
//        return mapper.toDomain(neo4j.save(mapper.toNode(message)));
//    }

//    @Override
//    public Optional<Message> findById(String id) {
//        return neo4j.findById(id).map(mapper::toDomain);
//    }

//    @Override
//    public List<Message> findByChatId(String chatId, int skip, int limit) {
//        return neo4j.findByChatIdOrderBySentAtDesc(chatId, skip, limit)
//                .stream().map(mapper::toDomain).toList();
//    }

    @Override
    public void markChatMessagesAsRead(String chatId, String readerId) {
        neo4j.markChatMessagesAsRead(chatId, readerId);
    }

    @Override
    public long countUnreadByChatId(String chatId, String userId) {
        return neo4j.countUnreadByChatId(chatId, userId);
    }
}
