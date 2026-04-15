package com.socialapp.infrastructure.adapter.persistence.neo4j.repository;

import com.socialapp.infrastructure.adapter.persistence.neo4j.node.PostNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostNeo4jRepository extends Neo4jRepository<PostNode, String> {

    /**
     * Lấy posts của author (paginated, chỉ load PostNode — không pull toàn bộ UserNode).
     * Graph: (User)-[:POSTED]->(Post)
     */
    @Query("""
        MATCH (u:User {id: $authorId})-[:POSTED]->(p:Post)
        WHERE p.deletedAt IS NULL
        RETURN p
        ORDER BY p.createdAt DESC
        SKIP $offset LIMIT $limit
        """)
    List<PostNode> findByAuthorId(@Param("authorId") String authorId,
                                  @Param("limit")    int limit,
                                  @Param("offset")   int offset);

    /**
     * Feed: PUBLIC posts của tất cả + FRIENDS posts của bạn bè + bài của chính mình.
     * Graph:
     *   (User)-[:POSTED]->(Post)
     *   (User)-[:FRIEND]-(User)
     *
     * Tối ưu: dùng Cypher UNION để phân tách rõ từng nhánh,
     * tránh cartesian product khi OR kết hợp OPTIONAL MATCH.
     */
    @Query("""
        MATCH (author:User)-[:POSTED]->(p:Post)
        WHERE p.deletedAt IS NULL
          AND (
            p.privacy = 'PUBLIC'
            OR author.id = $userId
            OR (
              p.privacy = 'FRIENDS'
              AND exists((author)-[:FRIEND]-(:User {id: $userId}))
            )
          )
        RETURN p
        ORDER BY p.createdAt DESC
        SKIP $offset LIMIT $limit
        """)
    List<PostNode> findFeedForUser(@Param("userId") String userId,
                                   @Param("limit")  int limit,
                                   @Param("offset") int offset);

    /**
     * Kiểm tra user đã like post chưa.
     * Graph: (User)-[:LIKED]->(Post)
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:LIKED]->(p:Post {id: $postId})
        RETURN count(*) > 0
        """)
    boolean hasLiked(@Param("userId") String userId,
                     @Param("postId") String postId);

    /**
     * Tạo LIKED relationship trực tiếp, không load node.
     * Graph: (User)-[:LIKED]->(Post)
     */
    @Query("""
        MATCH (u:User {id: $userId}), (p:Post {id: $postId})
        MERGE (u)-[:LIKED]->(p)
        """)
    void addLike(@Param("userId") String userId,
                 @Param("postId") String postId);

    /**
     * Xóa LIKED relationship trực tiếp.
     */
    @Query("""
        MATCH (u:User {id: $userId})-[r:LIKED]->(p:Post {id: $postId})
        DELETE r
        """)
    void removeLike(@Param("userId") String userId,
                    @Param("postId") String postId);

    /**
     * Tạo POSTED relationship khi tạo bài mới.
     * Graph: (User)-[:POSTED]->(Post)
     */
    @Query("""
        MATCH (u:User {id: $authorId}), (p:Post {id: $postId})
        MERGE (u)-[:POSTED]->(p)
        """)
    void linkAuthor(@Param("authorId") String authorId,
                    @Param("postId")   String postId);

    /**
     * Tìm kiếm post theo keyword qua HAS_KEYWORDS relationship.
     * Graph: (Post)-[:HAS_KEYWORDS]->(Keyword)
     * Dùng @Query vì cần traverse 2 tầng + filter text.
     */
    @Query("""
        MATCH (p:Post)-[:HAS_KEYWORDS]->(k:Keyword)
        WHERE p.deletedAt IS NULL
          AND toLower(k.text) CONTAINS toLower($keyword)
        RETURN DISTINCT p
        ORDER BY p.createdAt DESC
        SKIP $offset LIMIT $limit
        """)
    List<PostNode> searchByKeyword(@Param("keyword") String keyword,
                                   @Param("limit")   int limit,
                                   @Param("offset")  int offset);

    // ── Stats ─────────────────────────────────────────────────────────────

    @Query("MATCH (p:Post) WHERE p.deletedAt IS NULL RETURN count(p)")
    long countTotal();

    @Query("""
        MATCH (p:Post)
        WHERE p.createdAt >= $from AND p.createdAt <= $to
          AND p.deletedAt IS NULL
        RETURN count(p)
        """)
    long countNewPosts(@Param("from") LocalDateTime from,
                       @Param("to")   LocalDateTime to);

    @Query("""
        MATCH (p:Post)
        WHERE p.deletedAt >= $from AND p.deletedAt <= $to
        RETURN count(p)
        """)
    long countDeletedPosts(@Param("from") LocalDateTime from,
                           @Param("to")   LocalDateTime to);

    @Query("MATCH (p:Post) RETURN count(p)")
    long countAll();
}