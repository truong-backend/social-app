package com.socialapp.presentation.controller;

import com.socialapp.application.notification.usecase.*;
import com.socialapp.application.notification.dto.response.NotificationResponse;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final GetNotificationsUseCase           getNotificationsUseCase;
    private final MarkNotificationReadUseCase       markReadUseCase;
    private final MarkAllNotificationsReadUseCase   markAllReadUseCase;
    private final DeleteNotificationUseCase         deleteUseCase;
    private final CountUnreadNotificationsUseCase   countUnreadUseCase;
    private final AccountRepository                 accountRepository;

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

    /** GET /api/notifications/unread-count */
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> countUnread() {
        long count = countUnreadUseCase.execute(resolveUserId());
        return ApiResponse.ok(Map.of("count", count));
    }

    /** PATCH /api/notifications/{id}/read */
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable String id) {
        markReadUseCase.execute(resolveUserId(), id);
        return ApiResponse.ok("Marked as read");
    }

    /** PATCH /api/notifications/read-all */
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        markAllReadUseCase.execute(resolveUserId());
        return ApiResponse.ok("All marked as read");
    }

    /** DELETE /api/notifications/{id} */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        deleteUseCase.execute(resolveUserId(), id);
        return ApiResponse.ok("Deleted");
    }
}