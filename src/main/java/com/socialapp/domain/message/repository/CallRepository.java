package com.socialapp.domain.message.repository;

import com.socialapp.domain.message.entity.Call;

import java.util.Optional;

public interface CallRepository {
    Call save(Call call);
    Optional<Call> findByCallId(String callId);
    Optional<Call> findActiveCallByChatId(String chatId);
}