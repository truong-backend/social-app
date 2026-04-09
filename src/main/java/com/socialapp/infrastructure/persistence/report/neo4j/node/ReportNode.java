package com.socialapp.infrastructure.persistence.report.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("ReportNode")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportNode {

    @Id
    private String id;
    private String reporterId;
    private String targetType;
    private String targetId;
    private String reason;
    private String status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}