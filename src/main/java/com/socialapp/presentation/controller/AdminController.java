package com.socialapp.presentation.controller;

<<<<<<< HEAD
import com.socialapp.application.dto.request.AdminStatsRequest;
import com.socialapp.application.dto.request.PageRequest;
import com.socialapp.application.dto.response.*;
import com.socialapp.application.usecase.admin.AdminDeleteCommentUseCase;
import com.socialapp.application.usecase.admin.AdminDeletePostUseCase;
import com.socialapp.application.usecase.admin.AdminGetStatsUseCase;
import com.socialapp.application.usecase.admin.AdminListPostsUseCase;
import com.socialapp.application.usecase.admin.AdminListUsersUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller — Admin
 *
 * Tất cả endpoints yêu cầu role ADMIN (được enforce ở SecurityConfig + @PreAuthorize).
 *
 * GET    /api/admin/stats                                     — Thống kê tổng hợp
 * GET    /api/admin/users?q=&page=&size=                      — Danh sách / tìm kiếm người dùng
 * GET    /api/admin/posts?q=&page=&size=                      — Danh sách / tìm kiếm bài viết
 * DELETE /api/admin/posts/{postId}                            — Xóa bài viết (không cần là author)
 * DELETE /api/admin/posts/{postId}/comments/{commentId}       — Xóa bình luận (không cần là author)
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminGetStatsUseCase      adminGetStatsUseCase;
    private final AdminListUsersUseCase     adminListUsersUseCase;
    private final AdminListPostsUseCase     adminListPostsUseCase;
    private final AdminDeletePostUseCase    adminDeletePostUseCase;
    private final AdminDeleteCommentUseCase adminDeleteCommentUseCase;

    public AdminController(AdminGetStatsUseCase adminGetStatsUseCase,
                           AdminListUsersUseCase adminListUsersUseCase,
                           AdminListPostsUseCase adminListPostsUseCase,
                           AdminDeletePostUseCase adminDeletePostUseCase,
                           AdminDeleteCommentUseCase adminDeleteCommentUseCase) {
        this.adminGetStatsUseCase      = adminGetStatsUseCase;
        this.adminListUsersUseCase     = adminListUsersUseCase;
        this.adminListPostsUseCase     = adminListPostsUseCase;
        this.adminDeletePostUseCase    = adminDeletePostUseCase;
        this.adminDeleteCommentUseCase = adminDeleteCommentUseCase;
    }

    // ── GET /api/admin/stats ─────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        var data = adminGetStatsUseCase.execute(new AdminStatsRequest(from, to));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/admin/users ─────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> listUsers(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var data = adminListUsersUseCase.execute(q, new PageRequest(page, size));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── GET /api/admin/posts ─────────────────────────────────
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> listPosts(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var data = adminListPostsUseCase.execute(q, new PageRequest(page, size));

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── DELETE /api/admin/posts/{postId} ─────────────────────
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String postId) {

        adminDeletePostUseCase.execute(postId);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    // ── DELETE /api/admin/posts/{postId}/comments/{commentId}
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId) {

        adminDeleteCommentUseCase.execute(postId, commentId);

        return ResponseEntity.ok(ApiResponse.ok());
=======
import com.socialapp.application.admin.usecase.GetPostStatisticsUseCase;
import com.socialapp.application.admin.usecase.GetUserStatisticsUseCase;
import com.socialapp.domain.admin.valueobject.Statistics;
import com.socialapp.presentation.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GetUserStatisticsUseCase getUserStatisticsUseCase;
    private final GetPostStatisticsUseCase getPostStatisticsUseCase;

    /** GET /api/admin/statistics/users */
    @GetMapping("/statistics/users")
    public ApiResponse<Statistics> userStats() {
        return ApiResponse.ok(getUserStatisticsUseCase.execute());
    }

    /** GET /api/admin/statistics/posts */
    @GetMapping("/statistics/posts")
    public ApiResponse<Statistics> postStats() {
        return ApiResponse.ok(getPostStatisticsUseCase.execute());
>>>>>>> origin/master
    }
}