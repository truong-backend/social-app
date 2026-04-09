package com.socialapp.infrastructure.persistence.account.neo4j.repository;

import com.socialapp.infrastructure.persistence.account.neo4j.node.RefreshTokenNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenNeo4jRepository extends Neo4jRepository<RefreshTokenNode, String> {

    Optional<RefreshTokenNode> findByToken(String token);

    @Query("MATCH (r:RefreshTokenNode {accountId: $accountId}) SET r.revoked = true")
    void revokeAllByAccountId(@Param("accountId") String accountId);

    @Query("MATCH (r:RefreshTokenNode) WHERE r.expiresAt < datetime() DELETE r")
    void deleteExpired();
}