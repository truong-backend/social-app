package com.socialapp.domain.repository;

import com.socialapp.domain.model.aggregate.Chat;
import com.socialapp.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository: Chat
 */
public interface ChatRepository {

    Optional<Chat> findById(String id);

    /** Lấy tất cả đoạn chat mà user tham gia */
    List<Chat> findByMemberId(UserId userId);

    /** Tìm đoạn chat riêng tư giữa 2 người */
    Optional<Chat> findPrivateChat(UserId userA, UserId userB);

    void save(Chat chat);

    void delete(String id);
}