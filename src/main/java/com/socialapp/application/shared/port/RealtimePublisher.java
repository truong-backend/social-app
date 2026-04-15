package com.socialapp.application.shared.port;

/**
 * Outbound Port: RealtimePublisher
 * Được implement ở infrastructure (WebSocket / SSE)
 * Dùng để push real-time notification / message đến client
 */
public interface RealtimePublisher {
    void publishToUser(String userId, String eventType, Object payload);
    void publishToChat(String chatId, String eventType, Object payload);
    void publish(String destination, Object payload);
}