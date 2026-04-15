package com.socialapp.application.report.usecase;

import com.socialapp.application.report.dto.ReportDtos;
import com.socialapp.domain.report.entity.Report;
import com.socialapp.domain.report.repository.ReportRepository;
import org.springframework.transaction.annotation.Transactional;

public class CreateReportUseCase {

    private final ReportRepository reportRepository;

    public CreateReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReportDtos.ReportResponse execute(String reporterId,
                                             ReportDtos.CreateReportRequest request) {
        Report report = Report.create(
                reporterId,
                request.targetType(),
                request.targetId(),
                request.reason()
        );
        Report saved = reportRepository.save(report);
        return toResponse(saved);
    }

    static ReportDtos.ReportResponse toResponse(Report r) {
        return new ReportDtos.ReportResponse(
                r.getId(), r.getReporterId(),
                r.getTargetType().name(), r.getTargetId(),
                r.getReason(), r.getStatus().name(),
                r.getAdminNote(), r.getCreatedAt(), r.getResolvedAt()
        );
    }
}