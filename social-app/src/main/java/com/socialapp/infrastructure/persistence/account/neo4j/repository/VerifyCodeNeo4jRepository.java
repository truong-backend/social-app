package com.socialapp.infrastructure.persistence.account.neo4j.repository;

import com.socialapp.infrastructure.persistence.account.neo4j.node.VerifyCodeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifyCodeNeo4jRepository extends Neo4jRepository<VerifyCodeNode, String> {
    // findById(code) được kế thừa từ Neo4jRepository
}
