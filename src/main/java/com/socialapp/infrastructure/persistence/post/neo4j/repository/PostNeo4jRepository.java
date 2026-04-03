package com.socialapp.infrastructure.persistence.post.neo4j.repository;

import com.socialapp.infrastructure.persistence.post.neo4j.node.PostNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostNeo4jRepository extends Neo4jRepository<PostNode, String> {

    @Query("MATCH (p:Post {id: $id}) WHERE p.deletedAt IS NULL RETURN p")
    Optional<PostNode> findByIdNotDeleted(String id);

    // Feed: bài của bạn bè
    @Query("""
           MATCH (u:User {id: $userId})-[:FRIEND]-(friend:User)-[:POSTED]->(p:Post)
           WHERE p.deletedAt IS NULL AND p.privacy IN ['PUBLIC', 'FRIENDS']
           RETURN p ORDER BY p.createdAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<PostNode> findFeedByUserId(String userId, int skip, int limit);

    // Bài của một tác giả, lọc theo privacy
    @Query("""
           MATCH (author:User {id: $authorId})-[:POSTED]->(p:Post)
           WHERE p.deletedAt IS NULL
             AND (p.authorId = $viewerId
                  OR p.privacy = 'PUBLIC'
                  OR (p.privacy = 'FRIENDS'
                      AND EXISTS((author)-[:FRIEND]-(:User {id: $viewerId}))))
           RETURN p ORDER BY p.createdAt DESC
           SKIP $skip LIMIT $limit
           """)
    List<PostNode> findByAuthorId(String authorId, String viewerId, int skip, int limit);

    // Tìm kiếm theo keyword, loại trừ block
    @Query("""
           MATCH (p:Post)-[:HAS_KEYWORD]->(k:Keyword)
           WHERE k.text CONTAINS $keyword
             AND p.deletedAt IS NULL
             AND p.privacy = 'PUBLIC'
             AND NOT EXISTS((:User)-[:POSTED]->(p)<-[:BLOCK]-(:User {id: $requesterId}))
             AND NOT EXISTS((:User {id: $requesterId})-[:BLOCK]->(:User)-[:POSTED]->(p))
           RETURN DISTINCT p
           """)
    List<PostNode> searchByKeyword(String keyword, String requesterId);

    @Query("""
           MATCH (u:User {id: $userId})-[:LIKED]->(p:Post {id: $postId})
           RETURN count(u) > 0
           """)
    boolean isLikedByUser(String userId, String postId);

    // ── Context resolvers ────────────────────────────────────

    // Lấy originalPostId qua (Post)-[:SHARE]→(Post)
    @Query("""
           MATCH (shared:Post {id: $postId})-[:SHARE]->(original:Post)
           RETURN original.id
           """)
    Optional<String> findSharedFromPostId(String postId);

    // Lấy danh sách file đính kèm qua (Post)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (p:Post {id: $postId})-[:ATTACH_FILE]->(f:File)
           RETURN f.path
           """)
    List<String> findAttachedFilePathsByPostId(String postId);

    // Lấy danh sách keywords qua (Post)-[:HAS_KEYWORD]→(Keyword)
    @Query("""
           MATCH (p:Post {id: $postId})-[:HAS_KEYWORD]->(k:Keyword)
           RETURN k.text
           """)
    List<String> findKeywordsByPostId(String postId);

    // ── Relationships ────────────────────────────────────────

    // (User)-[:POSTED]→(Post)
    @Query("""
           MATCH (u:User {id: $authorId}), (p:Post {id: $postId})
           MERGE (u)-[:POSTED]->(p)
           """)
    void linkAuthorToPost(String authorId, String postId);

    // (User)-[:LIKED]→(Post)
    @Query("""
           MATCH (u:User {id: $userId}), (p:Post {id: $postId})
           MERGE (u)-[:LIKED]->(p)
           """)
    void linkUserLikedPost(String userId, String postId);

    // xóa LIKED khi unlike
    @Query("""
           MATCH (u:User {id: $userId})-[r:LIKED]->(p:Post {id: $postId})
           DELETE r
           """)
    void unlinkUserLikedPost(String userId, String postId);

    // (Post)-[:SHARE]→(Post) — sharedPost → originalPost
    @Query("""
           MATCH (shared:Post {id: $sharedPostId}), (original:Post {id: $originalPostId})
           MERGE (shared)-[:SHARE]->(original)
           """)
    void linkSharePost(String sharedPostId, String originalPostId);

    // (Post)-[:ATTACH_FILE]→(File)
    @Query("""
           MATCH (p:Post {id: $postId}), (f:File {path: $filePath})
           MERGE (p)-[:ATTACH_FILE]->(f)
           """)
    void linkPostAttachFile(String postId, String filePath);

    // (Post)-[:HAS_KEYWORD]→(Keyword) — MERGE tạo Keyword node nếu chưa có
    @Query("""
           MATCH (p:Post {id: $postId})
           MERGE (k:Keyword {text: $keyword})
           MERGE (p)-[:HAS_KEYWORD]->(k)
           """)
    void linkPostKeyword(String postId, String keyword);

    // (User)-[:INTERACT_WITH]→(Keyword)
    @Query("""
           MERGE (k:Keyword {text: $keyword})
           WITH k
           MATCH (u:User {id: $userId})
           MERGE (u)-[:INTERACT_WITH]->(k)
           """)
    void linkUserInteractKeyword(String userId, String keyword);
}
