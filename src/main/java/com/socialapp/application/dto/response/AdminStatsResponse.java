package com.socialapp.application.dto.response;

import java.time.LocalDate;

public record AdminStatsResponse(
        LocalDate from,
        LocalDate to,
        long      totalUsers,
        long      newUsers,
        long      totalPosts,
        long      newPosts,
        long      totalComments,
        long      deletedPosts
) {}