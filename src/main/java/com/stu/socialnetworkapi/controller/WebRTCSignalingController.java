package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.request.WebRTCSignalRequest;
import com.stu.socialnetworkapi.dto.response.WebRTCSignalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebRTC Signaling qua STOMP WebSocket.
 * Client gửi tới /app/signal → server forward tới /signal/{toUsername}
 * Không cần Stringee SDK.
 */
@Controller
@RequiredArgsConstructor
public class WebRTCSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/signal")
    public void relay(@Payload WebRTCSignalRequest req, Principal principal) {
        String fromUsername = principal.getName(); // đây là userId (UUID) từ JWT

        WebRTCSignalResponse response = WebRTCSignalResponse.builder()
                .from(fromUsername)
                .type(req.type())
                .payload(req.payload())
                .isVideoCall(false) // sẽ được parse ở client từ payload nếu cần
                .build();

        messagingTemplate.convertAndSend(
                WebSocketChannelPrefix.SIGNAL_CHANNEL_PREFIX + "/" + req.to(),
                response
        );
    }
}