package com.socialapp.domain.message.entity;

import com.socialapp.domain.message.exception.MessageDomainException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity: Call (subclass of Message)
 * Đại diện cho cuộc gọi trong chat — kế thừa Message,
 * thêm thông tin từ Stringee API.
 */
public class Call extends Message {

    private final String callId;          // Mã từ Stringee API
    private LocalDateTime callAt;
    private LocalDateTime endAt;
    private boolean isAnswered;
    private final boolean isVideoCall;

    private Call(String id, String senderId, String chatId,
                 String content, List<String> filePaths,
                 LocalDateTime sentAt,
                 String callId, LocalDateTime callAt,
                 LocalDateTime endAt, boolean isAnswered, boolean isVideoCall) {
//        super(Message.reconstitute(id, senderId, chatId, content,
//                filePaths, false, null, null, sentAt, sentAt));
        super(id, senderId, chatId, content, filePaths,
                false, null, null, sentAt, sentAt);
        this.callId      = callId;
        this.callAt      = callAt;
        this.endAt       = endAt;
        this.isAnswered  = isAnswered;
        this.isVideoCall = isVideoCall;
    }

    // ── Factory ───────────────────────────────────────────────

    public static Call initiate(String senderId, String chatId,
                                String callId, boolean isVideoCall) {
        return new Call(UUID.randomUUID().toString(), senderId, chatId,
                isVideoCall ? "[Video Call]" : "[Voice Call]",
                List.of(), LocalDateTime.now(),
                callId, LocalDateTime.now(), null, false, isVideoCall);
    }

    public static Call reconstitute(String id, String senderId, String chatId,
                                    String callId, LocalDateTime callAt,
                                    LocalDateTime endAt, boolean isAnswered,
                                    boolean isVideoCall, LocalDateTime sentAt) {
        return new Call(id, senderId, chatId,
                isVideoCall ? "[Video Call]" : "[Voice Call]",
                List.of(), sentAt,
                callId, callAt, endAt, isAnswered, isVideoCall);
    }

    // ── Domain Behaviors ──────────────────────────────────────

    public void answer() {
        if (isAnswered) throw new MessageDomainException("Call already answered");
        this.isAnswered = true;
        this.callAt     = LocalDateTime.now();
    }

    public void end() {
        if (endAt != null) throw new MessageDomainException("Call already ended");
        this.endAt = LocalDateTime.now();
    }

    // ── Getters ───────────────────────────────────────────────

    public String getCallId()          { return callId; }
    public LocalDateTime getCallAt()   { return callAt; }
    public LocalDateTime getEndAt()    { return endAt; }
    public boolean isAnswered()        { return isAnswered; }
    public boolean isVideoCall()       { return isVideoCall; }
    public boolean isEnded()           { return endAt != null; }
}
