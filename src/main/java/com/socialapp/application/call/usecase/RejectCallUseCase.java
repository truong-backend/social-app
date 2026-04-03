package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.CallEndedPayload;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.infrastructure.call.InCallStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Callee từ chối cuộc gọi.
 * Push call_ended về phía caller.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RejectCallUseCase {

    private final InCallStore       inCallStore;
    private final RealtimePublisher realtimePublisher;
    private final AccountRepository accountRepository;

    /**
     * @param callId        ID cuộc gọi bị từ chối
     * @param callerUserId  userId của người gọi (để push thông báo về)
     */
    public void execute(String callId, String callerUserId) {
        // Xóa trạng thái prepared nếu có
        inCallStore.endCall(callId);

        // Push call_ended đến caller
        if (callerUserId != null && !callerUserId.isBlank()) {
            String callerAccountId = accountRepository.findByUserId(callerUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Caller account not found"))
                    .getId();

            realtimePublisher.publishToUser(
                    callerAccountId,
                    "call_ended",
                    new CallEndedPayload(callId)
            );
            log.info("Call rejected: callId={}, caller notified via accountId={}", callId, callerAccountId);
        }
    }
}
