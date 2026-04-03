package com.socialapp.domain.message.repository;

import com.socialapp.domain.message.entity.Call;

import java.util.Optional;

public interface CallRepository {

    Optional<Call> findByCallId(String callId);

    Call save(Call call);
}
