package com.socialapp.domain.report.repository;

import com.socialapp.domain.report.entity.Report;
import com.socialapp.domain.report.valueobject.ReportStatus;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> findById(String id);
    List<Report> findByStatus(ReportStatus status, int skip, int limit);
    List<Report> findAll(int skip, int limit);
}