package com.socialapp.infrastructure.persistence.comment.neo4j.repository;

import com.socialapp.infrastructure.persistence.comment.neo4j.node.CommentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentNeo4jRepository extends Neo4jRepository<CommentNode, String> {

    // Root comments của Post (chưa có REPLIED → comment nào)
    @Query("""
           MATCH (p:Post {id: $postId})-[:HAS_COMMENT]->(c:Comment)
           WHERE NOT EXISTS((c)-[:REPLIED]->(:Comment))
           RETURN c ORDER BY c.createdAt ASC
           SKIP $skip LIMIT $limit
           """)
    List<CommentNode> findRootByPostId(String postId, int skip, int limit);

    // Replies của một comment
    @Query("""
           MATCH (r:Comment)-[:REPLIED]->(c:Comment {id: $commentId})
           RETURN r ORDER BY r.createdAt ASC
           SKIP $skip LIMIT $limit
           """)
    List<CommentNode> findRepliesByCommentId(String commentId, int skip, int limit);

    @Query("""
           MATCH (u:User {id: $userId})-[:LIKED]->(c:Comment {id: $commentId})
           RETURN count(u) > 0
           """)
    boolean isLikedByUser(String userId, String commentId);

    // ── Context resolvers ────────────────────────────────────

    // Lấy authorId qua (User)-[:COMMENTED]→(Comment)
    @Query("""
           MATCH (u:User)-[:COMMENTED]->(c:Comment {id: $commentId})
           RETURN u.id
           """)
    String findAuthorIdByCommentId(String commentId);

    // Lấy postId qua (Post)-[:HAS_COMMENT]→(Comment)
    @Query("""
           MATCH (p:Post)-[:HAS_COMMENT]->(c:Comment {id: $commentId})
           RETURN p.id
           """)
    String findPostIdByCommentId(String commentId);

    // Lấy repliedToCommentId qua (Comment)-[:REPLIED]→(Comment)
    @Query("""
           MATCH (c:Comment {id: $commentId})-[:REPLIED]->(parent:Comment)
           RETURN parent.id
           """)
    String findRepliedToCommentId(String commentId);

    // Lấy danh sách file đính kèm qua (Comment)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (c:Comment {id: $commentId})-[:ATTACH_FILE]->(f:File)
           RETURN f.path
           """)
    List<String> findAttachedFilePathsByCommentId(String commentId);

    // ── Relationships ────────────────────────────────────────

    // (Post)-[:HAS_COMMENT]→(Comment)
    @Query("""
           MATCH (p:Post {id: $postId}), (c:Comment {id: $commentId})
           MERGE (p)-[:HAS_COMMENT]->(c)
           """)
    void linkPostToComment(String postId, String commentId);

    // (User)-[:COMMENTED]→(Comment)
    @Query("""
           MATCH (u:User {id: $userId}), (c:Comment {id: $commentId})
           MERGE (u)-[:COMMENTED]->(c)
           """)
    void linkUserCommented(String userId, String commentId);

    // (Comment)-[:REPLIED]→(Comment) — reply → parent
    @Query("""
           MATCH (reply:Comment {id: $replyId}), (parent:Comment {id: $parentId})
           MERGE (reply)-[:REPLIED]->(parent)
           """)
    void linkCommentReplied(String replyId, String parentId);

    // (User)-[:LIKED]→(Comment)
    @Query("""
           MATCH (u:User {id: $userId}), (c:Comment {id: $commentId})
           MERGE (u)-[:LIKED]->(c)
           """)
    void linkUserLikedComment(String userId, String commentId);

    // xóa LIKED khi unlike
    @Query("""
           MATCH (u:User {id: $userId})-[r:LIKED]->(c:Comment {id: $commentId})
           DELETE r
           """)
    void unlinkUserLikedComment(String userId, String commentId);

    // (Comment)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (c:Comment {id: $commentId}), (f:File {path: $filePath})
           MERGE (c)-[:ATTACH_FILE]->(f)
           """)
    void linkCommentAttachFile(String commentId, String filePath);
}
