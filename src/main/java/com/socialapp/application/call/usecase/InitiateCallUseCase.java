package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.InitiateCallRequest;
import com.socialapp.application.call.dto.CallDtos.InitiateCallResponse;
import com.socialapp.application.call.dto.CallDtos.IncomingCallPayload;
import com.socialapp.application.shared.exception.ConflictException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.infrastructure.call.InCallStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Bước 1: Caller nhấn nút gọi → server:
 *   1. Kiểm tra cả 2 không đang trong call khác
 *   2. Chuẩn bị trạng thái "prepared" (Stringee sắp khởi tạo call)
 *   3. Push WebSocket incoming_call đến callee để hiện modal
 *   4. Trả về callId tạm thời cho FE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitiateCallUseCase {

    private final UserRepository       userRepository;
    private final AccountRepository    accountRepository;
    private final InCallStore          inCallStore;
    private final RealtimePublisher    realtimePublisher;

    public InitiateCallResponse execute(String callerUserId, InitiateCallRequest request) {
        // Validate caller không đang trong call
        if (inCallStore.isInCall(callerUserId)) {
            throw new ConflictException("You are already in a call");
        }

        // Validate callee không đang trong call
        if (inCallStore.isInCall(request.targetUserId())) {
            throw new ConflictException("Target user is already in a call");
        }

        // Load caller info để lấy tên
        User caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Caller user not found"));

        // Verify callee tồn tại
        userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        // Tạo callId tạm (Stringee sẽ tạo callId thực khi bắt đầu)
        String tempCallId = "call-" + UUID.randomUUID();

        // Đánh dấu "prepared" — Stringee sắp gọi
        inCallStore.prepare(callerUserId, request.targetUserId());

        // Lấy accountId của callee để push WebSocket (convertAndSendToUser dùng accountId)
        String calleeAccountId = accountRepository.findByUserId(request.targetUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found"))
                .getId();

        // Push incoming_call đến callee
        IncomingCallPayload payload = new IncomingCallPayload(
                tempCallId,
                callerUserId,
                caller.getFullName().getDisplayName(),
                request.isVideoCall()
        );
        realtimePublisher.publishToUser(calleeAccountId, "incoming_call", payload);

        log.info("Call initiated: caller={}, callee={}, callId={}, video={}",
                callerUserId, request.targetUserId(), tempCallId, request.isVideoCall());

        return new InitiateCallResponse(tempCallId);
    }
}
