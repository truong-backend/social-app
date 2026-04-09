package com.socialapp.presentation.controller;

import com.socialapp.application.call.dto.CallDtos;
import com.socialapp.application.call.usecase.EndCallUseCase;
import com.socialapp.application.call.usecase.InitiateCallUseCase;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final InitiateCallUseCase initiateCallUseCase;
    private final EndCallUseCase      endCallUseCase;
    private final AccountRepository   accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /**
     * POST /api/calls/{targetUserId}
     * Khởi tạo cuộc gọi tới người dùng khác
     */
    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CallDtos.CallResponse> initiateCall(
            @PathVariable String targetUserId,
            @Valid @RequestBody CallDtos.InitiateCallRequest request) {
        return ApiResponse.ok(
                initiateCallUseCase.execute(resolveUserId(), targetUserId,
                        request.isVideoCall()));
    }

    /**
     * PATCH /api/calls/{callId}/end
     * Kết thúc cuộc gọi
     */
    @PatchMapping("/{callId}/end")
    public ApiResponse<Void> endCall(@PathVariable String callId) {
        endCallUseCase.execute(resolveUserId(), callId);
        return ApiResponse.ok("Call ended");
    }
}