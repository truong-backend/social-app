package com.socialapp.application.call.dto;

import java.time.LocalDateTime;

public class CallDtos {

    // ── Requests ──────────────────────────────────────────────

    /** FE gọi khi muốn khởi tạo cuộc gọi (trước khi Stringee bắt đầu) */
    public record InitiateCallRequest(String targetUserId, boolean isVideoCall) {}

    /** FE gọi khi muốn kết thúc hoặc reject cuộc gọi */
    public record EndCallRequest(String targetUserId) {}

    // ── Stringee Webhook DTOs ─────────────────────────────────

    public record StringeeUserDto(String type, String number, String alias) {}

    public record StringeeCallEvent(
            String type,
            String call_id,
            String call_status,
            boolean isVideoCall,
            StringeeUserDto from,
            StringeeUserDto to
    ) {}

    // ── Responses ─────────────────────────────────────────────

    public record InitiateCallResponse(String callId) {}

    public record StringeeTokenResponse(String token) {}

    /** Payload push qua WebSocket khi có incoming call */
    public record IncomingCallPayload(
            String callId,
            String callerId,
            String callerName,
            boolean isVideoCall
    ) {}

    /** Payload push qua WebSocket khi call kết thúc */
    public record CallEndedPayload(String callId) {}

    /** Response Stringee SCCO (answer URL) */
    public record StringeeSccoResponse(
            String action,
            String eventUrl,
            String format,
            StringeeSccoUser from,
            StringeeSccoUser to,
            Integer timeout,
            Integer maxConnectTime,
            Boolean peerToPeerCall,
            String customData
    ) {}

    public record StringeeSccoUser(String type, String number, String alias) {}

    /** Thông tin call đầy đủ (cho tin nhắn trong chat) */
    public record CallMessageResponse(
            String id,
            String callId,
            String senderId,
            String chatId,
            boolean isVideoCall,
            boolean isAnswered,
            LocalDateTime callAt,
            LocalDateTime endAt,
            LocalDateTime sentAt
    ) {}
}
