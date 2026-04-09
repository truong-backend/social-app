package com.socialapp.application.report.dto;

import com.socialapp.domain.report.valueobject.ReportStatus;
import com.socialapp.domain.report.valueobject.ReportTargetType;

import java.time.LocalDateTime;

public class ReportDtos {

    public record CreateReportRequest(
            ReportTargetType targetType,
            String targetId,
            String reason
    ) {}

    public record ResolveReportRequest(
            ReportStatus status,
            String adminNote
    ) {}

    public record ReportResponse(
            String id,
            String reporterId,
            String targetType,
            String targetId,
            String reason,
            String status,
            String adminNote,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt
    ) {}
}