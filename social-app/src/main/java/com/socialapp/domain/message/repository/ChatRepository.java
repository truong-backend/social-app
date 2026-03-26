package com.socialapp.domain.message.repository;

import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.entity.Message;

import java.util.List;
import java.util.Optional;

public interface ChatRepository {

    Optional<Chat> findById(String id);

    Optional<Chat> findDirectChatBetween(String userIdA, String userIdB);

    List<Chat> findByUserId(String userId);

    List<Chat> searchByUserId(String query, String userId);

    Chat save(Chat chat);
}
