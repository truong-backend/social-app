package com.socialapp.infrastructure.persistence.report.neo4j;

import com.socialapp.domain.report.entity.Report;
import com.socialapp.domain.report.repository.ReportRepository;
import com.socialapp.domain.report.valueobject.ReportStatus;
import com.socialapp.domain.report.valueobject.ReportTargetType;
import com.socialapp.infrastructure.persistence.report.neo4j.node.ReportNode;
import com.socialapp.infrastructure.persistence.report.neo4j.repository.ReportNeo4jRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final ReportNeo4jRepository neo4jRepository;

    @Override
    public Report save(Report report) {
        return toDomain(neo4jRepository.save(toNode(report)));
    }

    @Override
    public Optional<Report> findById(String id) {
        return neo4jRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Report> findByStatus(ReportStatus status, int skip, int limit) {
        return neo4jRepository.findByStatus(status.name(), skip, limit)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Report> findAll(int skip, int limit) {
        return neo4jRepository.findAllPaged(skip, limit)
                .stream().map(this::toDomain).toList();
    }

    private Report toDomain(ReportNode node) {
        return Report.reconstitute(
                node.getId(), node.getReporterId(),
                ReportTargetType.valueOf(node.getTargetType()),
                node.getTargetId(), node.getReason(),
                ReportStatus.valueOf(node.getStatus()),
                node.getAdminNote(), node.getCreatedAt(), node.getResolvedAt()
        );
    }

    private ReportNode toNode(Report r) {
        return ReportNode.builder()
                .id(r.getId())
                .reporterId(r.getReporterId())
                .targetType(r.getTargetType().name())
                .targetId(r.getTargetId())
                .reason(r.getReason())
                .status(r.getStatus().name())
                .adminNote(r.getAdminNote())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}