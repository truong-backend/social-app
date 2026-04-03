package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.EndCallRequest;
import com.socialapp.application.call.dto.CallDtos.CallEndedPayload;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.infrastructure.call.InCallStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Kết thúc cuộc gọi từ phía FE (cả caller lẫn callee đều có thể gọi).
 * Push call_ended WebSocket đến bên kia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EndCallUseCase {

    private final InCallStore       inCallStore;
    private final RealtimePublisher realtimePublisher;
    private final AccountRepository accountRepository;

    public void execute(String callId, EndCallRequest request) {
        // Xóa trạng thái in-call nếu có
        inCallStore.endCall(callId);

        // Push call_ended đến bên kia nếu có targetUserId
        if (request != null && request.targetUserId() != null && !request.targetUserId().isBlank()) {
            String targetAccountId = accountRepository.findByUserId(request.targetUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target user not found"))
                    .getId();

            realtimePublisher.publishToUser(
                    targetAccountId,
                    "call_ended",
                    new CallEndedPayload(callId)
            );
            log.info("call_ended pushed to accountId={}, callId={}", targetAccountId, callId);
        }
    }
}
