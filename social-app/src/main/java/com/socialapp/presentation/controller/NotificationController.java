package com.socialapp.presentation.controller;

import com.socialapp.application.notification.usecase.GetNotificationsUseCase;
import com.socialapp.application.notification.usecase.NotificationResponse;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final AccountRepository       accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /** GET /api/notifications */
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0")  int skip,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(
                getNotificationsUseCase.execute(resolveUserId(), skip, limit));
    }
}