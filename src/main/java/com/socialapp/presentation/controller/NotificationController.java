package com.socialapp.presentation.controller;

<<<<<<< HEAD
import com.socialapp.application.dto.response.ApiResponse;
import com.socialapp.application.dto.response.NotificationResponse;
import com.socialapp.application.usecase.notification.GetNotificationsUseCase;
import com.socialapp.application.usecase.notification.MarkNotificationReadUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
=======
import com.socialapp.application.notification.usecase.*;
import com.socialapp.application.notification.dto.response.NotificationResponse;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
>>>>>>> origin/master
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

<<<<<<< HEAD
    private final GetNotificationsUseCase     getNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
=======
    private final GetNotificationsUseCase           getNotificationsUseCase;
    private final MarkNotificationReadUseCase       markReadUseCase;
    private final MarkAllNotificationsReadUseCase   markAllReadUseCase;
    private final DeleteNotificationUseCase         deleteUseCase;
    private final CountUnreadNotificationsUseCase   countUnreadUseCase;
    private final AccountRepository                 accountRepository;
>>>>>>> origin/master

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