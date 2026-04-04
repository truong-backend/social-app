package com.socialapp.application.call.dto;

import java.time.LocalDateTime;

public class CallDtos {

    // ── Requests ──────────────────────────────────────────────

    /** FE gọi khi muốn khởi tạo cuộc gọi */
    public record InitiateCallRequest(String targetUserId, boolean isVideoCall) {}

    /** FE gọi khi muốn kết thúc hoặc reject cuộc gọi */
    public record EndCallRequest(String targetUserId) {}

    // ── Responses ─────────────────────────────────────────────

    public record InitiateCallResponse(String callId) {}

    /** ZegoCloud token response */
    public record ZegoTokenResponse(String token) {}

    /** Payload push qua WebSocket khi có incoming call */
    public record IncomingCallPayload(
            String callId,
            String callerId,
            String callerName,
            boolean isVideoCall
    ) {}

    /** Payload push qua WebSocket khi call kết thúc */
    public record CallEndedPayload(String callId) {}

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
