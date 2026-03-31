package com.socialapp.infrastructure.persistence.user.neo4j.repository;

import com.socialapp.infrastructure.persistence.user.neo4j.node.UserNode;
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

    // FIX: tách alias requester ra khỏi alias u để tránh nhầm lẫn trong EXISTS()
    @Query("""
            MATCH (requester:User {id: $requesterId})
            MATCH (u:User)
            WHERE (u.username CONTAINS $keyword
                   OR u.familyName CONTAINS $keyword
                   OR u.givenName  CONTAINS $keyword)
              AND u.id <> $requesterId
              AND NOT EXISTS((requester)-[:BLOCK]->(u))
              AND NOT EXISTS((u)-[:BLOCK]->(requester))
            RETURN u
            SKIP $skip LIMIT $limit
            """)
    List<UserNode> searchByKeyword(String keyword, String requesterId, int skip, int limit);

    // (User)-[:HAS_PROFILE_PICTURE]→(File) — xóa quan hệ cũ trước khi tạo mới
    @Query("""
           MATCH (u:User {id: $userId}), (f:File {path: $filePath})
           OPTIONAL MATCH (u)-[old:HAS_PROFILE_PICTURE]->(:File)
           DELETE old
           MERGE (u)-[:HAS_PROFILE_PICTURE]->(f)
           """)
    void linkProfilePicture(String userId, String filePath);
}
