package com.socialapp.domain.report.entity;

import com.socialapp.domain.report.valueobject.ReportStatus;
import com.socialapp.domain.report.valueobject.ReportTargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public class Report {

    private final String          id;
    private final String          reporterId;
    private final ReportTargetType targetType;
    private final String          targetId;
    private final String          reason;
    private       ReportStatus    status;
    private       String          adminNote;
    private final LocalDateTime   createdAt;
    private       LocalDateTime   resolvedAt;

    private Report(String id, String reporterId, ReportTargetType targetType,
                   String targetId, String reason, ReportStatus status,
                   String adminNote, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id         = id;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId   = targetId;
        this.reason     = reason;
        this.status     = status;
        this.adminNote  = adminNote;
        this.createdAt  = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static Report create(String reporterId, ReportTargetType targetType,
                                String targetId, String reason) {
        return new Report(UUID.randomUUID().toString(), reporterId, targetType,
                targetId, reason, ReportStatus.PENDING,
                null, LocalDateTime.now(), null);
    }

    public static Report reconstitute(String id, String reporterId,
                                      ReportTargetType targetType, String targetId,
                                      String reason, ReportStatus status,
                                      String adminNote, LocalDateTime createdAt,
                                      LocalDateTime resolvedAt) {
        return new Report(id, reporterId, targetType, targetId, reason,
                status, adminNote, createdAt, resolvedAt);
    }

    public void resolve(ReportStatus newStatus, String note) {
        this.status     = newStatus;
        this.adminNote  = note;
        this.resolvedAt = LocalDateTime.now();
    }

    public String getId()                    { return id; }
    public String getReporterId()            { return reporterId; }
    public ReportTargetType getTargetType()  { return targetType; }
    public String getTargetId()              { return targetId; }
    public String getReason()                { return reason; }
    public ReportStatus getStatus()          { return status; }
    public String getAdminNote()             { return adminNote; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getResolvedAt()     { return resolvedAt; }
}