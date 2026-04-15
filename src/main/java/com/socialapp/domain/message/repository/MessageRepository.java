package com.socialapp.domain.message.repository;

import com.socialapp.domain.message.entity.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    Optional<Message> findById(String id);

    List<Message> findByChatId(String chatId, int skip, int limit);

    Message save(Message message);

    void deleteById(String id);


    void markChatMessagesAsRead(String chatId, String readerId);
    long countUnreadByChatId(String chatId, String userId);
}
