package com.socialapp.infrastructure.persistence.post.mapper;

import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.valueobject.PostCounts;
import com.socialapp.domain.post.valueobject.Privacy;
import com.socialapp.infrastructure.persistence.post.neo4j.node.PostNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PostNode không còn lưu: sharedFromPostId, attachedFilePaths, keywords.
 * Các giá trị đó được quản lý hoàn toàn qua relationship:
 *   (Post)-[:SHARE]→(Post)
 *   (Post)-[:ATTACH_FILE]→(File)
 *   (Post)-[:HAS_KEYWORD]→(Keyword)
 *
 * Khi toDomain(), các field này được truyền vào từ PostRepositoryAdapter.
 */
@Component
public class PostMapper {

    /**
     * Map đơn giản từ node — sharedFromPostId, attachedFilePaths, keywords = empty.
     * Dùng khi adapter không cần resolve các relationship đó (ví dụ: feed, search).
     */
    public Post toDomain(PostNode n) {
        return toDomain(n, null, List.of(), List.of());
    }

    /**
     * Map đầy đủ khi adapter đã resolve sharedFromPostId, attachedFilePaths, keywords từ graph.
     */
    public Post toDomain(PostNode n, String sharedFromPostId,
                         List<String> attachedFilePaths, List<String> keywords) {
        return Post.reconstitute(
                n.getId(),
                n.getAuthorId(),
                n.getContent(),
                Privacy.valueOf(n.getPrivacy()),
                sharedFromPostId,
                attachedFilePaths != null ? attachedFilePaths : List.of(),
                keywords != null ? keywords : List.of(),
                PostCounts.of(
                        orZero(n.getLikeCount()),
                        orZero(n.getShareCount()),
                        orZero(n.getCommentCount())
                ),
                parse(n.getCreatedAt()),
                parse(n.getUpdatedAt()),
                parse(n.getDeletedAt())
        );
    }

    public PostNode toNode(Post p) {
        return PostNode.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
                .content(p.getContent())
                .privacy(p.getPrivacy().name())
                .likeCount(p.getCounts().getLikeCount())
                .shareCount(p.getCounts().getShareCount())
                .commentCount(p.getCounts().getCommentCount())
                .createdAt(str(p.getCreatedAt()))
                .updatedAt(str(p.getUpdatedAt()))
                .deletedAt(str(p.getDeletedAt()))
                .build();
    }

    private int orZero(Integer v)           { return v == null ? 0 : v; }
    private LocalDateTime parse(String s)   { return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)    { return dt == null ? null : dt.toString(); }
}
