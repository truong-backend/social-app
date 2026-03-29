package com.socialapp.infrastructure.persistence.comment.neo4j.repository;

import com.socialapp.infrastructure.persistence.comment.neo4j.node.CommentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentNeo4jRepository extends Neo4jRepository<CommentNode, String> {

    @Query("""
           MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
           WHERE c.repliedToCommentId IS NULL
           RETURN c ORDER BY c.createdAt ASC
           SKIP $skip LIMIT $limit
           """)
    List<CommentNode> findRootByPostId(String postId, int skip, int limit);

    @Query("""
           MATCH (c:Comment {id: $commentId})<-[:REPLIED]-(r:Comment)
           RETURN r ORDER BY r.createdAt ASC
           SKIP $skip LIMIT $limit
           """)
    List<CommentNode> findRepliesByCommentId(String commentId, int skip, int limit);

    @Query("""
           MATCH (u:User {id: $userId})-[:LIKED]->(c:Comment {id: $commentId})
           RETURN count(u) > 0
           """)
    boolean isLikedByUser(String userId, String commentId);
}
