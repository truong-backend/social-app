package com.socialapp.presentation.controller;

import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.NotificationResponse;
import com.socialapp.application.usecase.notification.GetNotificationsUseCase;
import com.socialapp.application.usecase.notification.MarkNotificationReadUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller — Notifications
 *
 * GET    /api/notifications           — Lấy tất cả thông báo
 * GET    /api/notifications/unread    — Lấy thông báo chưa đọc
 * PUT    /api/notifications/{id}/read — Đánh dấu một thông báo đã đọc
 * PUT    /api/notifications/read-all  — Đánh dấu tất cả thông báo đã đọc
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final GetNotificationsUseCase     getNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    public NotificationController(
            GetNotificationsUseCase getNotificationsUseCase,
            MarkNotificationReadUseCase markNotificationReadUseCase) {

        this.getNotificationsUseCase     = getNotificationsUseCase;
        this.markNotificationReadUseCase = markNotificationReadUseCase;
    }

    // ── GET /api/notifications ───────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(
            @AuthenticationPrincipal String userId) {

        List<NotificationResponse> data =
                getNotificationsUseCase.execute(userId);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/notifications/unread ────────────────────────
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(
            @AuthenticationPrincipal String userId) {

        List<NotificationResponse> data =
                getNotificationsUseCase.executeUnreadOnly(userId);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── PUT /api/notifications/{id}/read ─────────────────────
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal String userId,
            @PathVariable String notificationId) {

        markNotificationReadUseCase.execute(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── PUT /api/notifications/read-all ─────────────────────
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal String userId) {

        markNotificationReadUseCase.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}