package com.socialapp.infrastructure.persistence.user.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNode, String> {

    @Query("MATCH (u:User {username: $username}) RETURN u")
    Optional<UserNode> findByUsername(String username);

    @Query("MATCH (u:User {username: $username}) RETURN count(u) > 0")
    boolean existsByUsername(String username);

    @Query("""
            MATCH (u:User)
            WHERE (u.username CONTAINS $keyword OR u.familyName CONTAINS $keyword
                   OR u.givenName CONTAINS $keyword)
              AND u.id <> $requesterId
              AND NOT EXISTS {
                    MATCH (blocker:User {id: $requesterId})-[:BLOCK]->(u)
                  }
              AND NOT EXISTS {
                    MATCH (u)-[:BLOCK]->(requester:User {id: $requesterId})
                  }
            RETURN u
            SKIP $skip LIMIT $limit
            """)
    List<UserNode> searchByKeyword(String keyword, String requesterId, int skip, int limit);
}