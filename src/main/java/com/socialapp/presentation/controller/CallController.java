package com.socialapp.presentation.controller;

import com.socialapp.application.call.dto.CallDtos.*;
import com.socialapp.application.call.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho tất cả call endpoints.
 *
 * Endpoints:
 *   GET  /api/calls/zego-token            → lấy ZegoCloud token cho FE
 *   POST /api/calls                       → khởi tạo cuộc gọi
 *   POST /api/calls/{callId}/reject       → callee từ chối
 *   POST /api/calls/{callId}/end          → kết thúc cuộc gọi
 */
@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final AccountRepository   accountRepository;
    private final GetZegoTokenUseCase getZegoTokenUseCase;
    private final InitiateCallUseCase initiateCallUseCase;
    private final RejectCallUseCase   rejectCallUseCase;
    private final EndCallUseCase      endCallUseCase;

    /** Lấy userId từ JWT → AccountRepository */
    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow()
                .getUserId();
    }

    /**
     * GET /api/calls/zego-token
     * FE gọi khi init ZegoExpressEngine.
     */
    @GetMapping("/zego-token")
    public ApiResponse<ZegoTokenResponse> getZegoToken() {
        String userId = resolveUserId();
        return ApiResponse.ok(getZegoTokenUseCase.execute(userId));
    }

    /**
     * POST /api/calls
     * Caller nhấn nút gọi → server push incoming_call đến callee.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InitiateCallResponse> initiateCall(
            @Valid @RequestBody InitiateCallRequest request) {
        String callerUserId = resolveUserId();
        return ApiResponse.ok(initiateCallUseCase.execute(callerUserId, request));
    }

    /**
     * POST /api/calls/{callId}/reject?callerUserId=xxx
     * Callee từ chối → server push call_ended đến caller.
     */
    @PostMapping("/{callId}/reject")
    public ApiResponse<Void> rejectCall(
            @PathVariable String callId,
            @RequestParam String callerUserId) {
        rejectCallUseCase.execute(callId, callerUserId);
        return ApiResponse.ok("Call rejected");
    }

    /**
     * POST /api/calls/{callId}/end
     * Kết thúc cuộc gọi — cả caller và callee đều có thể gọi.
     */
    @PostMapping("/{callId}/end")
    public ApiResponse<Void> endCall(
            @PathVariable String callId,
            @RequestBody(required = false) EndCallRequest request) {
        endCallUseCase.execute(callId, request);
        return ApiResponse.ok("Call ended");
    }
}
