package com.socialapp.infrastructure.persistence.post.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostNeo4jRepository extends Neo4jRepository<PostNode, String> {

    @Query("MATCH (p:Post {id: $id}) WHERE p.deletedAt IS NULL RETURN p")
    Optional<PostNode> findByIdNotDeleted(String id);

    @Query("""
           MATCH (u:User {id: $userId})-[:FRIEND]-(friend:User)-[:POSTED]->(p:Post)
           WHERE p.deletedAt IS NULL AND p.privacy IN ['PUBLIC','FRIENDS']
           RETURN p ORDER BY p.createdAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<PostNode> findFeedByUserId(String userId, int skip, int limit);

    @Query("""
           MATCH (author:User {id: $authorId})-[:POSTED]->(p:Post)
           WHERE p.deletedAt IS NULL
             AND (p.authorId = $viewerId
                  OR p.privacy = 'PUBLIC'
                  OR (p.privacy = 'FRIENDS' AND EXISTS {
                        MATCH (v:User {id: $viewerId})-[:FRIEND]-(author)
                      }))
           RETURN p ORDER BY p.createdAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<PostNode> findByAuthorId(String authorId, String viewerId, int skip, int limit);

    @Query("""
           MATCH (p:Post)-[:HAS_KEYWORDS]->(k:Keyword)
           WHERE k.text CONTAINS $keyword
             AND p.deletedAt IS NULL
             AND p.privacy = 'PUBLIC'
             AND NOT EXISTS {
                   MATCH (author:User)-[:POSTED]->(p)
                   WHERE (author)-[:BLOCK]->(:User {id: $requesterId})
                      OR (:User {id: $requesterId})-[:BLOCK]->(author)
                 }
           RETURN DISTINCT p
           """)
    List<PostNode> searchByKeyword(String keyword, String requesterId);

    @Query("""
           MATCH (u:User {id: $userId})-[:LIKED]->(p:Post {id: $postId})
           RETURN count(u) > 0
           """)
    boolean isLikedByUser(String userId, String postId);
}