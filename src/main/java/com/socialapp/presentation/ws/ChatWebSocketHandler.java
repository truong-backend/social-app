package com.socialapp.presentation.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialapp.application.dto.request.SendMessageRequest;
import com.socialapp.application.dto.response.CallResponse;
import com.socialapp.application.dto.response.MessageResponse;
import com.socialapp.application.port.TokenPort;
import com.socialapp.application.usecase.chat.AnswerCallUseCase;
import com.socialapp.application.usecase.chat.EndCallUseCase;
import com.socialapp.application.usecase.chat.SendMessageUseCase;
import com.socialapp.application.usecase.chat.StartCallUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket (STOMP) handler cho Chat real-time & Call signaling.
 *
 * ════════════════════════════════════════════════════════════════
 *  KẾT NỐI
 * ════════════════════════════════════════════════════════════════
 *   Endpoint:  ws://host/ws
 *   SockJS:    http://host/ws (fallback tự động)
 *   Auth:      Gửi JWT trong header STOMP CONNECT:
 *                  Authorization: Bearer <token>
 *
 * ════════════════════════════════════════════════════════════════
 *  SUBSCRIBE (Client → Server)
 * ════════════════════════════════════════════════════════════════
 *   /user/queue/messages          — Nhận tin nhắn mới (private)
 *   /user/queue/calls             — Nhận sự kiện cuộc gọi (private)
 *   /topic/chat/{chatId}          — Broadcast tin nhắn trong chat room
 *
 * ════════════════════════════════════════════════════════════════
 *  SEND (Client → Server via /app)
 * ════════════════════════════════════════════════════════════════
 *   /app/chat.send                — Gửi tin nhắn { chatId, content }
 *   /app/chat.call.start/{chatId} — Bắt đầu cuộc gọi { isVideo: bool }
 *   /app/chat.call.answer/{chatId}/{callId} — Trả lời cuộc gọi
 *   /app/chat.call.end/{chatId}/{callId}    — Kết thúc cuộc gọi
 *
 * ════════════════════════════════════════════════════════════════
 *  FLOW GỬI TIN NHẮN
 * ════════════════════════════════════════════════════════════════
 *   Client A gửi   → /app/chat.send
 *   Server xử lý   → SendMessageUseCase (lưu DB)
 *   Server broadcast:
 *       → /topic/chat/{chatId}         (tất cả members)
 *       → /user/{recipientId}/queue/messages (user cụ thể)
 *
 * ════════════════════════════════════════════════════════════════
 *  FLOW CALL SIGNALING
 * ════════════════════════════════════════════════════════════════
 *   Caller  → /app/chat.call.start/{chatId}
 *   Server  → /user/{calleeId}/queue/calls  (INCOMING_CALL event)
 *   Callee  → /app/chat.call.answer/{chatId}/{callId}
 *   Server  → /user/{callerId}/queue/calls  (CALL_ANSWERED event)
 *   Either  → /app/chat.call.end/{chatId}/{callId}
 *   Server  → /user/{otherId}/queue/calls   (CALL_ENDED event)
 *
 *   Media (audio/video) thực hiện qua WebRTC / third-party SDK
 *   (callId từ CallResponse là ID của cuộc gọi trong hệ thống bên ngoài).
 */
@Controller
public class ChatWebSocketHandler {

    private final SendMessageUseCase    sendMessageUseCase;
    private final StartCallUseCase      startCallUseCase;
    private final AnswerCallUseCase     answerCallUseCase;
    private final EndCallUseCase        endCallUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketHandler(SendMessageUseCase sendMessageUseCase,
                                StartCallUseCase startCallUseCase,
                                AnswerCallUseCase answerCallUseCase,
                                EndCallUseCase endCallUseCase,
                                SimpMessagingTemplate messagingTemplate) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.startCallUseCase   = startCallUseCase;
        this.answerCallUseCase  = answerCallUseCase;
        this.endCallUseCase     = endCallUseCase;
        this.messagingTemplate  = messagingTemplate;
    }

    // ── /app/chat.send ───────────────────────────────────────
    // Client gửi tin nhắn. Payload: { chatId, content }
    // Server lưu DB → broadcast đến tất cả members.
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest req,
                            Principal principal) {
        String userId = principal.getName();
        MessageResponse message = sendMessageUseCase.execute(userId, req);

        // 1. Broadcast đến room topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + req.chatId(),
                WsEvent.message(message));

        // 2. Delivery đến /user/{userId}/queue/messages nếu cần private delivery
        //    (client có thể subscribe song song cả hai)
    }

    // ── /app/chat.call.start/{chatId} ────────────────────────
    // Người gọi bắt đầu cuộc gọi.
    // Payload: { isVideo: boolean } (JSON body) hoặc header param
    @MessageMapping("/chat.call.start/{chatId}")
    public void startCall(@DestinationVariable String chatId,
                          @Payload CallStartPayload payload,
                          Principal principal) {
        String callerId = principal.getName();
        CallResponse call = startCallUseCase.execute(callerId, chatId,
                payload != null && payload.isVideo());

        // Gửi sự kiện INCOMING_CALL đến callee (otherUserId có thể lấy từ Chat)
        // Broadcast vào topic để tất cả member trong chat nhận
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                WsEvent.incomingCall(call));
    }

    // ── /app/chat.call.answer/{chatId}/{callId} ───────────────
    // Người nhận chấp nhận cuộc gọi.
    @MessageMapping("/chat.call.answer/{chatId}/{callId}")
    public void answerCall(@DestinationVariable String chatId,
                           @DestinationVariable String callId,
                           Principal principal) {
        answerCallUseCase.execute(chatId, callId);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                WsEvent.callAnswered(callId, principal.getName()));
    }

    // ── /app/chat.call.end/{chatId}/{callId} ─────────────────
    // Kết thúc cuộc gọi (bất kỳ bên nào).
    @MessageMapping("/chat.call.end/{chatId}/{callId}")
    public void endCall(@DestinationVariable String chatId,
                        @DestinationVariable String callId,
                        Principal principal) {
        endCallUseCase.execute(chatId, callId);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                WsEvent.callEnded(callId, principal.getName()));
    }

    // ── Helper: Payload record ────────────────────────────────

    /** Payload từ client khi bắt đầu cuộc gọi */
    public record CallStartPayload(boolean isVideo) {}

    // ── Inner class: WsEvent envelope ────────────────────────
    // Tất cả WebSocket message được bọc trong envelope có `type` field
    // để client dễ dispatch.

    public static class WsEvent {
        public String type;
        public Object data;

        private WsEvent(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        public static WsEvent message(MessageResponse msg) {
            return new WsEvent("NEW_MESSAGE", msg);
        }

        public static WsEvent incomingCall(CallResponse call) {
            return new WsEvent("INCOMING_CALL", call);
        }

        public static WsEvent callAnswered(String callId, String answeredBy) {
            return new WsEvent("CALL_ANSWERED",
                    new CallEventData(callId, answeredBy));
        }

        public static WsEvent callEnded(String callId, String endedBy) {
            return new WsEvent("CALL_ENDED",
                    new CallEventData(callId, endedBy));
        }

        public record CallEventData(String callId, String userId) {}
    }

    // ════════════════════════════════════════════════════════
    //  WebSocket Broker Configuration (inner @Configuration)
    // ════════════════════════════════════════════════════════

    /**
     * Cấu hình STOMP broker, prefix và JWT interceptor.
     * Đặt cùng file để dễ tham chiếu, tách ra WebSocketConfig.java nếu cần.
     */
    @Configuration
    @EnableWebSocketMessageBroker
    public static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

        private final TokenPort tokenPort;

        public WebSocketConfig(TokenPort tokenPort) {
            this.tokenPort = tokenPort;
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws")
                    .setAllowedOriginPatterns("*")
                    .withSockJS();          // SockJS fallback cho môi trường không hỗ trợ WS
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            // Client subscribe: /topic/... (broadcast) và /user/... (private)
            config.enableSimpleBroker("/topic", "/user");
            // Client gửi: /app/...
            config.setApplicationDestinationPrefixes("/app");
            // Prefix cho convertAndSendToUser
            config.setUserDestinationPrefix("/user");
        }

        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            // JWT interceptor: xác thực khi CONNECT
            registration.interceptors(new ChannelInterceptor() {
                @Override
                public Message<?> preSend(Message<?> message, MessageChannel channel) {
                    StompHeaderAccessor accessor =
                            MessageHeaderAccessor.getAccessor(
                                    message, StompHeaderAccessor.class);

                    if (accessor != null
                            && StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7).trim();
                            if (tokenPort.isValid(token)) {
                                String userId = tokenPort.extractUserId(token);
                                String role   = tokenPort.extractRole(token);
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                userId,
                                                null,
                                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                        );
                                accessor.setUser(auth);
                            }
                        }
                    }
                    return message;
                }
            });
        }
    }
}