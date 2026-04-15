package com.socialapp.infrastructure.adapter.persistence.neo4j.repository;

import com.socialapp.infrastructure.adapter.persistence.neo4j.node.AccountNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountNeo4jRepository extends Neo4jRepository<AccountNode, String> {

    // ── Derived method queries ────────────────────────────────────────────

    Optional<AccountNode> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Load AccountNode kèm VerifyCode (sửa bug: phải RETURN a, không phải RETURN v).
     * Graph: (Account)-[:HAS_VERIFY_CODE]->(VerifyCode)
     * SDN4j sẽ tự map VerifyCodeNode qua @Relationship HAS_VERIFY_CODE trên AccountNode.
     */
    @Query("""
        MATCH (a:Account {id: $accountId})-[:HAS_VERIFY_CODE]->(v:VerifyCode)
        RETURN a, collect(v) AS __verifyCode__
        """)
    Optional<AccountNode> findByIdWithVerifyCode(@Param("accountId") String accountId);
}