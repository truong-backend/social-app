package com.socialapp.presentation.controller;

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
    }
}