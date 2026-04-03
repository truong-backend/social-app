package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.StringeeCallEvent;
import com.socialapp.application.call.dto.CallDtos.CallMessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.message.entity.Call;
import com.socialapp.domain.message.entity.Chat;
import com.socialapp.domain.message.repository.CallRepository;
import com.socialapp.domain.message.repository.ChatRepository;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.valueobject.Username;
import com.socialapp.infrastructure.call.InCallStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * Xử lý Stringee event webhook (POST /api/stringee/event).
 *
 * Events quan trọng:
 *   stringee_call / started  → tạo Call entity, lưu DB, push chat message
 *   stringee_call / answered → đánh dấu isAnswered = true
 *   stringee_call / ended    → đặt endAt, xóa in-call state, push call_ended
 *
 * InCallStore dùng userId làm key (nhất quán với InitiateCallUseCase).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleStringeeEventUseCase {

    private final UserRepository    userRepository;
    private final ChatRepository    chatRepository;
    private final CallRepository    callRepository;
    private final AccountRepository accountRepository;
    private final InCallStore       inCallStore;
    private final RealtimePublisher realtimePublisher;

    private static final Map<String, String> SUCCESS = Map.of("status", "success");

    @Transactional
    public Map<String, String> execute(StringeeCallEvent event) {
        if (event.type() == null) return SUCCESS;
        if (!"stringee_call".equals(event.type())) {
            log.debug("Unknown Stringee event type: {}", event.type());
            return SUCCESS;
        }

        String callStatus     = event.call_status();
        String callId         = event.call_id();
        String callerUsername = event.from() != null ? event.from().number() : null;
        String calleeUsername = event.to()   != null ? event.to().number()   : null;

        if (callStatus == null) return SUCCESS;

        switch (callStatus) {
            case "started"  -> handleStarted(callId, callerUsername, calleeUsername, event.isVideoCall());
            case "ringing"  -> log.info("📞 Ringing — callId={}", callId);
            case "answered" -> handleAnswered(callId);
            case "ended"    -> handleEnded(callId);
            case "failed"   -> log.warn("❌ Call failed — callId={}", callId);
            case "busy"     -> log.info("📵 Busy — callId={}", callId);
            case "timeout"  -> log.info("⏰ Timeout — callId={}", callId);
            default         -> log.warn("❓ Unknown call_status={} callId={}", callStatus, callId);
        }

        return SUCCESS;
    }

    // ── Handlers ──────────────────────────────────────────────

    private void handleStarted(String callId, String callerUsername,
                               String calleeUsername, boolean isVideoCall) {
        User caller = userRepository.findByUsername(Username.of(callerUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Caller not found: " + callerUsername));
        User callee = userRepository.findByUsername(Username.of(calleeUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Callee not found: " + calleeUsername));

        String callerUserId = caller.getId();
        String calleeUserId = callee.getId();

        // isPreparedForCall dùng userId (giống InitiateCallUseCase.prepare)
        if (!inCallStore.isPreparedForCall(callerUserId, calleeUserId)
                && !inCallStore.isPreparedForCall(calleeUserId, callerUserId)) {
            log.warn("Call started but not prepared: caller={}, callee={}", callerUsername, calleeUsername);
        }

        // Lấy hoặc tạo direct chat
        Chat chat = chatRepository.findDirectChatBetween(callerUserId, calleeUserId)
                .orElseGet(() -> chatRepository.save(Chat.createDirect(callerUserId, calleeUserId)));

        // Tạo Call entity
        Call call = Call.initiate(callerUserId, chat.getId(), callId, isVideoCall);
        Call savedCall = callRepository.save(call);

        // Đánh dấu in-call (userId-based)
        inCallStore.markInCall(callerUserId, calleeUserId, callId);

        // Push call message vào chat topic
        CallMessageResponse msg = toResponse(savedCall);
        realtimePublisher.publishToChat(chat.getId(), "message", msg);

        // Push vào message queue của callee
        String calleeAccountId = accountRepository.findByUserId(calleeUserId)
                .map(a -> a.getId()).orElse(null);
        if (calleeAccountId != null) {
            realtimePublisher.publishToUser(calleeAccountId, "message", msg);
        }

        log.info("✅ Call started: callId={}, caller={}, callee={}", callId, callerUsername, calleeUsername);
    }

    private void handleAnswered(String callId) {
        callRepository.findByCallId(callId).ifPresent(call -> {
            call.answer();
            callRepository.save(call);
            log.info("✅ Call answered: callId={}", callId);
        });
    }

    private void handleEnded(String callId) {
        // Lấy userIds trước khi xóa
        Set<String> userIds = inCallStore.getMemberUserIds(callId);

        // Cập nhật DB
        callRepository.findByCallId(callId).ifPresent(call -> {
            call.end();
            callRepository.save(call);
        });

        // Xóa in-call state
        inCallStore.endCall(callId);

        // Push call_ended đến cả 2 user qua accountId
        userIds.forEach(userId -> {
            String accountId = accountRepository.findByUserId(userId)
                    .map(a -> a.getId()).orElse(null);
            if (accountId != null) {
                realtimePublisher.publishToUser(accountId, "call_ended",
                        Map.of("callId", callId));
            }
        });

        log.info("🔚 Call ended: callId={}", callId);
    }

    // ── Helpers ───────────────────────────────────────────────

    private CallMessageResponse toResponse(Call call) {
        return new CallMessageResponse(
                call.getId(),
                call.getCallId(),
                call.getSenderId(),
                call.getChatId(),
                call.isVideoCall(),
                call.isAnswered(),
                call.getCallAt(),
                call.getEndAt(),
                call.getSentAt()
        );
    }
}
