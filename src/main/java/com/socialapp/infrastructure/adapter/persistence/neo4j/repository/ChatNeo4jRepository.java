package com.socialapp.infrastructure.adapter.persistence.neo4j.repository;

import com.socialapp.infrastructure.adapter.persistence.neo4j.node.ChatNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatNeo4jRepository extends Neo4jRepository<ChatNode, String> {

    /**
     * Tất cả chat mà user tham gia, kèm messages + file attachments.
     * Graph:
     *   (User)-[:IS_MEMBER_OF]->(Chat)-[:HAS_MESSAGE]->(Message)-[:ATTACH_FILE]->(File)
     *
     * Dùng @Query vì cần traverse 3 tầng + custom projection để tránh N+1.
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(c:Chat)
        OPTIONAL MATCH (c)-[:HAS_MESSAGE]->(m:Message)
        OPTIONAL MATCH (m)-[:ATTACH_FILE]->(f:File)
        RETURN c,
               collect(DISTINCT m{.*,
                   __nodeLabels__: labels(m),
                   __elementId__: elementId(m),
                   Message_ATTACH_FILE_File_true: CASE WHEN f IS NOT NULL
                       THEN [f{.*, __nodeLabels__: labels(f), __elementId__: elementId(f)}]
                       ELSE [] END
               }) AS __messages__
        ORDER BY c.createdAt DESC
        """)
    List<ChatNode> findByMemberId(@Param("userId") String userId);

    /**
     * Chat riêng tư giữa 2 user (chỉ lấy chat mà cả hai cùng là member).
     * Graph: (User)-[:IS_MEMBER_OF]->(Chat)<-[:IS_MEMBER_OF]-(User)
     */
    @Query("""
        MATCH (a:User {id: $userAId})-[:IS_MEMBER_OF]->(c:Chat)<-[:IS_MEMBER_OF]-(b:User {id: $userBId})
        OPTIONAL MATCH (c)-[:HAS_MESSAGE]->(m:Message)
        OPTIONAL MATCH (m)-[:ATTACH_FILE]->(f:File)
        RETURN c,
               collect(DISTINCT m{.*,
                   __nodeLabels__: labels(m),
                   __elementId__: elementId(m),
                   Message_ATTACH_FILE_File_true: CASE WHEN f IS NOT NULL
                       THEN [f{.*, __nodeLabels__: labels(f), __elementId__: elementId(f)}]
                       ELSE [] END
               }) AS __messages__
        LIMIT 1
        """)
    Optional<ChatNode> findPrivateChat(@Param("userAId") String userAId,
                                       @Param("userBId") String userBId);

    /**
     * Kiểm tra user có phải member của chat không.
     * Graph: (User)-[:IS_MEMBER_OF]->(Chat)
     */
    @Query("""
        MATCH (u:User {id: $userId})-[:IS_MEMBER_OF]->(c:Chat {id: $chatId})
        RETURN count(u) > 0
        """)
    boolean isMember(@Param("chatId") String chatId,
                     @Param("userId") String userId);

    /**
     * Lấy danh sách userId của tất cả member trong một chat.
     * Graph: (User)-[:IS_MEMBER_OF]->(Chat)
     * Thay thế findAll().stream().filter() trong ChatRepositoryAdapter — loại bỏ full scan.
     */
    @Query("""
        MATCH (u:User)-[:IS_MEMBER_OF]->(c:Chat {id: $chatId})
        RETURN u.id
        """)
    List<String> findMemberIdsByChatId(@Param("chatId") String chatId);

    /**
     * Tạo IS_MEMBER_OF relationship trực tiếp trên graph.
     * Graph: (User)-[:IS_MEMBER_OF]->(Chat)
     * Dùng khi save Chat mới, thay vì load + mutate UserNode.chats.
     */
    @Query("""
        MATCH (u:User {id: $userId}), (c:Chat {id: $chatId})
        MERGE (u)-[:IS_MEMBER_OF]->(c)
        """)
    void addMember(@Param("chatId") String chatId,
                   @Param("userId") String userId);
}