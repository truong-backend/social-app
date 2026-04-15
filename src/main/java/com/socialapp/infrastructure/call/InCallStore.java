package com.socialapp.infrastructure.call;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store để track trạng thái cuộc gọi.
 * Thay thế Redis InCallRepository từ social-network.
 *
 * Tất cả key đều dùng userId (không dùng username)
 * để nhất quán giữa InitiateCallUseCase và call event handler.
 */
@Slf4j
@Component
public class InCallStore {

    // userId → callId
    private final Map<String, String> inCallMap = new ConcurrentHashMap<>();

    // "callerUserId:calleeUserId" → true
    private final Map<String, Boolean> preparedMap = new ConcurrentHashMap<>();

    // callId → Set<userId>
    private final Map<String, Set<String>> callUserIdsMap = new ConcurrentHashMap<>();

    // ── Prepare (trước khi ZegoCloud kết nối) ─────────────────

    /**
     * Đánh dấu cặp userId đã "prepared" — ZegoCloud sắp kết nối.
     */
    public void prepare(String callerUserId, String calleeUserId) {
        preparedMap.put(callerUserId + ":" + calleeUserId, true);
        log.debug("Prepared call: caller={}, callee={}", callerUserId, calleeUserId);
    }

    public boolean isPreparedForCall(String callerUserId, String calleeUserId) {
        return preparedMap.containsKey(callerUserId + ":" + calleeUserId);
    }

    public void clearPrepare(String callerUserId, String calleeUserId) {
        preparedMap.remove(callerUserId + ":" + calleeUserId);
        preparedMap.remove(calleeUserId + ":" + callerUserId);
    }

    // ── In-call state ─────────────────────────────────────────

    /**
     * Kiểm tra userId đang trong cuộc gọi không.
     */
    public boolean isInCall(String userId) {
        return inCallMap.containsKey(userId);
    }

    public Optional<String> getCallId(String userId) {
        return Optional.ofNullable(inCallMap.get(userId));
    }

    /**
     * Đánh dấu 2 user đang trong call.
     */
    public void markInCall(String callerUserId, String calleeUserId, String callId) {
        inCallMap.put(callerUserId, callId);
        inCallMap.put(calleeUserId, callId);
        Set<String> members = ConcurrentHashMap.newKeySet();
        members.add(callerUserId);
        members.add(calleeUserId);
        callUserIdsMap.put(callId, members);
        clearPrepare(callerUserId, calleeUserId);
        log.debug("markInCall: callId={}, caller={}, callee={}", callId, callerUserId, calleeUserId);
    }

    /**
     * Trả về userIds trong call.
     */
    public Set<String> getMemberUserIds(String callId) {
        return callUserIdsMap.getOrDefault(callId, Set.of());
    }

    /**
     * Kết thúc call, xóa toàn bộ state liên quan.
     */
    public void endCall(String callId) {
        Set<String> members = callUserIdsMap.remove(callId);
        if (members != null) {
            members.forEach(inCallMap::remove);
        }
        log.debug("endCall: callId={}", callId);
    }

    public void endCallByUserId(String userId) {
        String callId = inCallMap.get(userId);
        if (callId != null) {
            endCall(callId);
        }
    }

    // Trong InCallStore
    public void put(String userId, String callId) {
        inCallMap.put(userId, callId);
    }

    public void remove(String userId) {
        inCallMap.remove(userId);
    }


}
