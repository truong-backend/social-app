package com.socialapp.application.report.usecase;

import com.socialapp.application.report.dto.ReportDtos;
import com.socialapp.domain.report.repository.ReportRepository;
import com.socialapp.domain.report.valueobject.ReportStatus;

import java.util.List;

public class GetReportsUseCase {

    private final ReportRepository reportRepository;

    public GetReportsUseCase(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<ReportDtos.ReportResponse> execute(String statusFilter, int skip, int limit) {
        if (statusFilter != null && !statusFilter.isBlank()) {
            ReportStatus status = ReportStatus.valueOf(statusFilter.toUpperCase());
            return reportRepository.findByStatus(status, skip, limit)
                    .stream().map(CreateReportUseCase::toResponse).toList();
        }
        return reportRepository.findAll(skip, limit)
                .stream().map(CreateReportUseCase::toResponse).toList();
    }
}