package com.socialapp.infrastructure.realtime;

import com.socialapp.application.shared.port.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketRealtimePublisher implements RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishToUser(String userId, String eventType, Object payload) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/" + eventType.toLowerCase(),
                payload
        );
    }

    @Override
    public void publishToChat(String chatId, String eventType, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/chat." + chatId + "." + eventType.toLowerCase(),
                payload
        );
    }
}