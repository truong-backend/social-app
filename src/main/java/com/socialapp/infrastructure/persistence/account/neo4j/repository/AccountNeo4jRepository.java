package com.socialapp.infrastructure.persistence.account.neo4j.repository;

import com.socialapp.infrastructure.persistence.account.neo4j.node.AccountNode;
import com.socialapp.infrastructure.persistence.account.neo4j.node.VerifyCodeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountNeo4jRepository extends Neo4jRepository<AccountNode, String> {

    @Query("MATCH (a:Account {email: $email}) RETURN a")
    Optional<AccountNode> findByEmail(String email);

    @Query("MATCH (a:Account {email: $email}) RETURN count(a) > 0")
    boolean existsByEmail(String email);

    // Tìm Account qua relationship HAS_VERIFY_CODE → VerifyCode
    @Query("MATCH (a:Account)-[:HAS_VERIFY_CODE]->(v:VerifyCode {code: $code}) RETURN a")
    Optional<AccountNode> findByVerifyCode(String code);

    // Lấy VerifyCode node của Account
    @Query("MATCH (a:Account {id: $accountId})-[:HAS_VERIFY_CODE]->(v:VerifyCode) RETURN v.code AS code,v.isVerified AS isVerified, v.expiryTime AS expiryTime")
    Optional<VerifyCodeNode> findVerifyCodeByAccountId(String accountId);

    // (Account)-[:HAS_VERIFY_CODE]→(VerifyCode)
    @Query("""
           MATCH (a:Account {id: $accountId}), (v:VerifyCode {code: $code})
           OPTIONAL MATCH (a)-[old:HAS_VERIFY_CODE]->(:VerifyCode)
           DELETE old
           MERGE (a)-[:HAS_VERIFY_CODE]->(v)
           """)
    void linkAccountToVerifyCode(String accountId, String code);

    // (Account)-[:HAS_INFO]→(User)
    @Query("""
           MATCH (a:Account {id: $accountId}), (u:User {id: $userId})
           MERGE (a)-[:HAS_INFO]->(u)
           """)
    void linkAccountToUser(String accountId, String userId);


}
