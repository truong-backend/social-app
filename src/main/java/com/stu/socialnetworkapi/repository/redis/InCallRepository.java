package com.stu.socialnetworkapi.repository.redis;

import com.stu.socialnetworkapi.config.WebSocketChannelPrefix;
import com.stu.socialnetworkapi.dto.response.MessageCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InCallRepository {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String INCALL_KEY = "incall:";
    private static final String PREPARED_FOR_CALL_KEY = "prepared_for_call:";
    private static final String CALL_KEY = "call:";
    private static final String CALL_UUID_KEY = "call_uuid:";

    public void call(String caller, String callee, String callId, UUID callerId, UUID calleeId) {
        redisTemplate.opsForValue().set(INCALL_KEY + caller, callId);
        redisTemplate.opsForValue().set(INCALL_KEY + callee, callId);
        redisTemplate.opsForSet().add(CALL_KEY + callId, callee, caller);
        redisTemplate.opsForSet().add(CALL_UUID_KEY + callId, callerId.toString(), calleeId.toString());
    }

    public void prepare(String caller, String callee) {
        redisTemplate.opsForValue().set(PREPARED_FOR_CALL_KEY + caller + ":" + callee, "true", Duration.ofMinutes(2));
    }

    public boolean isPreparedForCall(String caller, String callee) {
        return redisTemplate != null && redisTemplate.hasKey(PREPARED_FOR_CALL_KEY + caller + ":" + callee);
    }

    public String getCallId(String user) {
        return redisTemplate.opsForValue().get(INCALL_KEY + user);
    }

    public Set<String> getMembers(String callId) {
        return redisTemplate.opsForSet().members(CALL_KEY + callId);
    }

    public void endCall(String callId) {
        try {
            Set<String> members = getMembers(callId);
            Set<String> memberIds = redisTemplate.opsForSet().members(CALL_UUID_KEY + callId);

            if (members == null || members.size() < 2 || memberIds == null || memberIds.size() < 2) {
                log.warn("Not enough members to end call, callId={}", callId);
                return;
            }

            String[] userArray = members.toArray(new String[0]);
            String[] userIdArray = memberIds.toArray(new String[0]);

            String user1 = userArray[0];
            String user2 = userArray[1];
            String userId1 = userIdArray[0];
            String userId2 = userIdArray[1];

            redisTemplate.delete(INCALL_KEY + user1);
            redisTemplate.delete(INCALL_KEY + user2);
            redisTemplate.delete(PREPARED_FOR_CALL_KEY + user1 + ":" + user2);
            redisTemplate.delete(PREPARED_FOR_CALL_KEY + user2 + ":" + user1);
            redisTemplate.delete(CALL_KEY + callId);
            redisTemplate.delete(CALL_UUID_KEY + callId);

            MessageCommand command = MessageCommand.builder()
                    .id(callId)
                    .command(MessageCommand.Command.END_CALL)
                    .build();

            // FIX: bỏ dấu "/" thừa trong topic — phải khớp với WebSocketChannelPrefix
            messagingTemplate.convertAndSend(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + userId1, command);
            messagingTemplate.convertAndSend(WebSocketChannelPrefix.MESSAGE_CHANNEL_PREFIX + userId2, command);
        } catch (Exception e) {
            log.error("Error in endCall, callId={}", callId, e);
        }
    }

    public void endCallByMemberUsername(String username) {
        String callId = getCallId(username);
        if (callId == null) {
            log.warn("No active call found for user: {}", username);
            return;
        }
        endCall(callId);
    }

    public boolean isInCall(String username) {
        return redisTemplate != null && redisTemplate.hasKey(INCALL_KEY + username);
    }

    public void cleanupPrepared(String caller, String callee) {
        try {
            redisTemplate.delete(PREPARED_FOR_CALL_KEY + caller + ":" + callee);
            redisTemplate.delete(PREPARED_FOR_CALL_KEY + callee + ":" + caller);
            redisTemplate.delete(INCALL_KEY + caller);
            redisTemplate.delete(INCALL_KEY + callee);
            log.info("Cleaned up call state for {} and {}", caller, callee);
        } catch (Exception e) {
            log.error("Error in cleanupPrepared", e);
        }
    }
}