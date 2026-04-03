package com.socialapp.application.call.usecase;

import com.socialapp.application.call.dto.CallDtos.StringeeSccoResponse;
import com.socialapp.application.call.dto.CallDtos.StringeeSccoUser;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.valueobject.Username;
import com.socialapp.infrastructure.call.InCallStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý Stringee Answer URL (GET /api/stringee/answer).
 * Stringee gọi URL này để biết cách kết nối cuộc gọi (SCCO).
 *
 * fromId / toId là username của caller/callee (từ Stringee).
 * InCallStore dùng userId → phải lookup userId trước khi check.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleStringeeAnswerUseCase {

    private final UserRepository userRepository;
    private final InCallStore    inCallStore;

    public List<StringeeSccoResponse> execute(
            String fromId,       // username của caller
            String toId,         // username của callee
            String callId,
            boolean fromInternal,
            boolean appToPhone,
            int timeout,
            int maxConnectTime,
            boolean peerToPeerCall,
            boolean isRecord,
            String recordFormat,
            boolean isVideoCall) {

        List<StringeeSccoResponse> sccoList = new ArrayList<>();

        // Load caller và callee bằng username
        User caller = userRepository.findByUsername(Username.of(fromId))
                .orElseThrow(() -> new ResourceNotFoundException("Caller not found: " + fromId));
        User callee = userRepository.findByUsername(Username.of(toId))
                .orElseThrow(() -> new ResourceNotFoundException("Callee not found: " + toId));

        // InCallStore dùng userId — kiểm tra callee có đang bận không
        if (inCallStore.isInCall(callee.getId())) {
            log.warn("Callee {} (userId={}) already in call, rejecting Stringee answer",
                    toId, callee.getId());
            return sccoList;
        }

        // Record action (optional)
        if (isRecord) {
            sccoList.add(new StringeeSccoResponse(
                    "record", "", recordFormat,
                    null, null, null, null, null, null
            ));
        }

        // Connect action
        StringeeSccoUser from = new StringeeSccoUser(
                fromInternal ? "internal" : "external",
                caller.getUsername().getValue(),
                caller.getFullName().getDisplayName()
        );
        StringeeSccoUser to = new StringeeSccoUser(
                appToPhone ? "external" : "internal",
                callee.getUsername().getValue(),
                callee.getFullName().getDisplayName()
        );

        sccoList.add(new StringeeSccoResponse(
                "connect",
                null,
                null,
                from,
                to,
                timeout,
                maxConnectTime,
                peerToPeerCall,
                caller.getProfilePicturePath()
        ));

        return sccoList;
    }
}
