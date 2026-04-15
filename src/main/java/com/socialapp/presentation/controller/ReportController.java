package com.socialapp.presentation.controller;

import com.socialapp.application.report.dto.ReportDtos;
import com.socialapp.application.report.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.presentation.util.ApiResponse;
import com.socialapp.presentation.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final CreateReportUseCase  createReportUseCase;
    private final GetReportsUseCase    getReportsUseCase;
    private final ResolveReportUseCase resolveReportUseCase;
    private final BanUserUseCase       banUserUseCase;
    private final UnbanUserUseCase     unbanUserUseCase;
    private final AccountRepository    accountRepository;

    private String resolveUserId() {
        return accountRepository.findById(SecurityUtil.currentAccountId())
                .orElseThrow().getUserId();
    }

    /** POST /api/reports — user báo cáo bài/người */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportDtos.ReportResponse> create(
            @Valid @RequestBody ReportDtos.CreateReportRequest request) {
        return ApiResponse.ok(createReportUseCase.execute(resolveUserId(), request));
    }

    /** GET /api/reports?status=PENDING&skip=0&limit=20 — admin xem danh sách */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReportDtos.ReportResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int skip,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(getReportsUseCase.execute(status, skip, limit));
    }

    /** PATCH /api/reports/{id}/resolve — admin xử lý report */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReportDtos.ReportResponse> resolve(
            @PathVariable String id,
            @Valid @RequestBody ReportDtos.ResolveReportRequest request) {
        return ApiResponse.ok(resolveReportUseCase.execute(id, request));
    }

    /** POST /api/reports/ban/{userId} — admin ban user */
    @PostMapping("/ban/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> ban(
            @PathVariable String userId,
            @RequestParam(defaultValue = "Violated community guidelines") String reason) {
        banUserUseCase.execute(userId, reason);
        return ApiResponse.ok("User banned");
    }

    /** DELETE /api/reports/ban/{userId} — admin unban user */
    @DeleteMapping("/ban/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unban(@PathVariable String userId) {
        unbanUserUseCase.execute(userId);
        return ApiResponse.ok("User unbanned");
    }
}