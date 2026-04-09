package com.socialapp.application.call.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.message.repository.CallRepository;
import com.socialapp.infrastructure.call.InCallStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public class EndCallUseCase {

    private final CallRepository    callRepository;
    private final RealtimePublisher realtimePublisher;
    private final InCallStore       inCallStore;

    public EndCallUseCase(CallRepository callRepository,
                          RealtimePublisher realtimePublisher,
                          InCallStore inCallStore) {
        this.callRepository    = callRepository;
        this.realtimePublisher = realtimePublisher;
        this.inCallStore       = inCallStore;
    }

    @Transactional
    public void execute(String userId, String callId) {
        var call = callRepository.findByCallId(callId)
                .orElseThrow(() -> new ResourceNotFoundException("Call not found"));

        call.end();
        callRepository.save(call);
        inCallStore.remove(call.getChatId());

        // Notify tất cả thành viên chat rằng call kết thúc
        realtimePublisher.publish(
                "/topic/call-ended/" + call.getChatId(),
                Map.of("callId", callId, "chatId", call.getChatId())
        );
    }
}