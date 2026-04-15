package com.socialapp.infrastructure.persistence.report.neo4j.repository;

import com.socialapp.infrastructure.persistence.report.neo4j.node.ReportNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportNeo4jRepository extends Neo4jRepository<ReportNode, String> {

    @Query("MATCH (r:ReportNode {status: $status}) " +
            "RETURN r ORDER BY r.createdAt DESC SKIP $skip LIMIT $limit")
    List<ReportNode> findByStatus(
            @Param("status") String status,
            @Param("skip")   int skip,
            @Param("limit")  int limit);

    @Query("MATCH (r:ReportNode) RETURN r ORDER BY r.createdAt DESC SKIP $skip LIMIT $limit")
    List<ReportNode> findAllPaged(
            @Param("skip")  int skip,
            @Param("limit") int limit);
}