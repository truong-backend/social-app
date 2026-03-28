package com.socialapp.infrastructure.account;

import com.socialapp.infrastructure.account.neo4j.AccountNode;
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
}
