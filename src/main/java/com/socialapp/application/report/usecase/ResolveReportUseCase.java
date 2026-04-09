package com.socialapp.application.report.usecase;

import com.socialapp.application.report.dto.ReportDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.report.repository.ReportRepository;
import com.socialapp.domain.report.valueobject.ReportStatus;
import org.springframework.transaction.annotation.Transactional;

public class ResolveReportUseCase {

    private final ReportRepository reportRepository;

    public ResolveReportUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReportDtos.ReportResponse execute(String reportId,
                                             ReportDtos.ResolveReportRequest request) {
        var report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.resolve(request.status(), request.adminNote());
        var saved = reportRepository.save(report);
        return CreateReportUseCase.toResponse(saved);
    }
}