package com.socialapp.infrastructure.adapter.persistence.neo4j.repository;

import com.socialapp.infrastructure.adapter.persistence.neo4j.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNode, String> {

    // ── Derived method queries (no @Query needed) ─────────────────────────

    Optional<UserNode> findByUsername(String username);

    boolean existsByUsername(String username);

    // ── @Query: full-text search (complex condition, cannot be derived) ───

    @Query("""
        MATCH (u:User)
        WHERE toLower(u.username)   CONTAINS toLower($keyword)
           OR toLower(u.familyName) CONTAINS toLower($keyword)
           OR toLower(u.givenName)  CONTAINS toLower($keyword)
        RETURN u
        ORDER BY u.username
        SKIP $offset LIMIT $limit
        """)
    List<UserNode> searchByKeyword(@Param("keyword") String keyword,
                                   @Param("limit")   int limit,
                                   @Param("offset")  int offset);

    // ── @Query: relationship predicates (cannot be expressed as derived) ──

    /**
     * Chat IDs mà user tham gia.
     * Graph: (User)-[:IS_MEMBER_OF]->(Chat)
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(c:Chat)
        RETURN c.id
        """)
    List<String> findChatIdsByUserId(@Param("userId") String userId);

    /**
     * Kiểm tra quan hệ bạn bè (undirected FRIEND).
     * Graph: (User)-[:FRIEND]-(User)
     */
    @Query("""
        MATCH (a:User {id: $userAId})-[:FRIEND]-(b:User {id: $userBId})
        RETURN count(*) > 0
        """)
    boolean areFriends(@Param("userAId") String userAId,
                       @Param("userBId") String userBId);

    /**
     * Kiểm tra block relationship.
     * Graph: (User)-[:BLOCK]->(User)
     */
    @Query("""
        MATCH (blocker:User {id: $blockerId})-[:BLOCK]->(target:User {id: $targetId})
        RETURN count(*) > 0
        """)
    boolean isBlocked(@Param("blockerId") String blockerId,
                      @Param("targetId")  String targetId);

    /**
     * Danh sách bạn bè (paginated).
     * Graph: (User)-[:FRIEND]-(User) — undirected
     */
    @Query("""
        MATCH (me:User {id: $userId})-[:FRIEND]-(friend:User)
        RETURN friend
        ORDER BY friend.familyName
        SKIP $offset LIMIT $limit
        """)
    List<UserNode> listFriends(@Param("userId") String userId,
                               @Param("limit")  int limit,
                               @Param("offset") int offset);

    /**
     * Danh sách lời mời đã gửi (paginated).
     * Graph: (User)-[:REQUEST]->(User)
     */
    @Query("""
        MATCH (me:User {id: $userId})-[:REQUEST]->(target:User)
        RETURN target
        ORDER BY target.familyName
        SKIP $offset LIMIT $limit
        """)
    List<UserNode> listSentRequests(@Param("userId") String userId,
                                    @Param("limit")  int limit,
                                    @Param("offset") int offset);

    /**
     * Danh sách lời mời đã nhận (paginated).
     * Graph: (sender:User)-[:REQUEST]->(me:User)
     */
    @Query("""
        MATCH (sender:User)-[:REQUEST]->(me:User {id: $userId})
        RETURN sender
        ORDER BY sender.familyName
        SKIP $offset LIMIT $limit
        """)
    List<UserNode> listReceivedRequests(@Param("userId") String userId,
                                        @Param("limit")  int limit,
                                        @Param("offset") int offset);

    /**
     * Kiểm tra đã gửi REQUEST chưa.
     * Graph: (User)-[:REQUEST]->(User)
     */
    @Query("""
        MATCH (me:User {id: $userId})-[:REQUEST]->(target:User {id: $targetId})
        RETURN count(*) > 0
        """)
    boolean hasSentRequest(@Param("userId")   String userId,
                           @Param("targetId") String targetId);

    /**
     * Kiểm tra đã nhận REQUEST chưa.
     * Graph: (sender:User)-[:REQUEST]->(me:User)
     */
    @Query("""
        MATCH (sender:User {id: $senderId})-[:REQUEST]->(me:User {id: $userId})
        RETURN count(*) > 0
        """)
    boolean hasReceivedRequest(@Param("userId")   String userId,
                               @Param("senderId") String senderId);

    // ── @Query: relationship mutations ───────────────────────────────────

    /**
     * Tạo REQUEST relationship trực tiếp trên graph, không load node.
     */
    @Query("""
        MATCH (sender:User {id: $senderId}), (receiver:User {id: $receiverId})
        MERGE (sender)-[:REQUEST]->(receiver)
        """)
    void createRequestRelationship(@Param("senderId")   String senderId,
                                   @Param("receiverId") String receiverId);

    /**
     * Xóa REQUEST relationship trực tiếp, không load node.
     */
    @Query("""
        MATCH (sender:User {id: $senderId})-[r:REQUEST]->(receiver:User {id: $receiverId})
        DELETE r
        """)
    void deleteRequestRelationship(@Param("senderId")   String senderId,
                                   @Param("receiverId") String receiverId);

    /**
     * Tạo FRIEND relationship (outgoing từ userA → userB).
     * Graph định nghĩa: User --FRIEND--> User (OUTGOING trên UserNode.friends)
     */
    @Query("""
        MATCH (a:User {id: $userAId}), (b:User {id: $userBId})
        MERGE (a)-[:FRIEND]->(b)
        """)
    void createFriendRelationship(@Param("userAId") String userAId,
                                  @Param("userBId") String userBId);

    /**
     * Xóa FRIEND relationship (cả hai chiều để đảm bảo consistency).
     */
    @Query("""
        MATCH (a:User {id: $userAId})-[r:FRIEND]-(b:User {id: $userBId})
        DELETE r
        """)
    void deleteFriendRelationship(@Param("userAId") String userAId,
                                  @Param("userBId") String userBId);

    /**
     * Tạo BLOCK relationship.
     * Graph: (User)-[:BLOCK]->(User)
     */
    @Query("""
        MATCH (blocker:User {id: $blockerId}), (target:User {id: $targetId})
        MERGE (blocker)-[:BLOCK]->(target)
        """)
    void createBlockRelationship(@Param("blockerId") String blockerId,
                                 @Param("targetId")  String targetId);

    /**
     * Xóa BLOCK relationship.
     */
    @Query("""
        MATCH (blocker:User {id: $blockerId})-[r:BLOCK]->(target:User {id: $targetId})
        DELETE r
        """)
    void deleteBlockRelationship(@Param("blockerId") String blockerId,
                                 @Param("targetId")  String targetId);

    // ── Stats ─────────────────────────────────────────────────────────────

    @Query("MATCH (u:User) RETURN count(u)")
    long countAll();
}