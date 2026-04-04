package com.socialapp.infrastructure.config;

import com.socialapp.infrastructure.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * STOMP ChannelInterceptor: đọc JWT từ CONNECT frame header
 * và set Principal lên WS session.
 *
 * Không có interceptor này → convertAndSendToUser() không tìm được
 * session theo accountId → message bị drop im lặng → callee không
 * nhận được incoming_call event.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("[WS] CONNECT frame has no valid Authorization header");
            return message;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("[WS] Invalid JWT in CONNECT frame");
                return message;
            }

            String accountId = jwtTokenProvider.extractAccountId(token);
            String role      = jwtTokenProvider.extractRole(token);

            // Set principal = accountId — khớp với JwtAuthenticationFilter
            // convertAndSendToUser(accountId, ...) sẽ tìm đúng session này
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            accountId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            accessor.setUser(auth);
            log.debug("[WS] CONNECT authenticated: accountId={}", accountId);

        } catch (Exception e) {
            log.error("[WS] JWT parse error in CONNECT frame: {}", e.getMessage());
        }

        return message;
    }
}
