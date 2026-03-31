package com.socialapp.infrastructure.persistence.comment.mapper;

import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.infrastructure.persistence.comment.neo4j.node.CommentNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CommentNode không còn lưu: authorId, postId, repliedToCommentId, attachedFilePaths.
 * Các giá trị đó được quản lý hoàn toàn qua relationship:
 *   (User)-[:COMMENTED]→(Comment)
 *   (Post)-[:HAS_COMMENT]→(Comment)
 *   (Comment)-[:REPLIED]→(Comment)
 *   (Comment)-[:ATTACH_FILE]→(File)
 *
 * Khi toDomain(), các field này được truyền vào từ CommentRepositoryAdapter
 * (đã biết context khi query), không lấy từ node.
 */
@Component
public class CommentMapper {

    /**
     * Map từ node — chỉ có các field lưu trong node.
     * authorId, postId, repliedToCommentId, attachedFilePaths cần được truyền riêng
     * từ adapter khi biết context.
     */
    public Comment toDomain(CommentNode n) {
        return toDomain(n, null, null, null, List.of());
    }

    /**
     * Map đầy đủ khi adapter có đủ context từ graph.
     */
    public Comment toDomain(CommentNode n, String authorId, String postId,
                            String repliedToCommentId, List<String> attachedFilePaths) {
        return Comment.reconstitute(
                n.getId(),
                authorId,
                postId,
                repliedToCommentId,
                n.getContent(),
                attachedFilePaths != null ? attachedFilePaths : List.of(),
                orZero(n.getLikeCount()),
                orZero(n.getReplyCount()),
                parse(n.getCreatedAt()),
                parse(n.getUpdatedAt())
        );
    }

    public CommentNode toNode(Comment c) {
        return CommentNode.builder()
                .id(c.getId())
                .content(c.getContent())
                .likeCount(c.getLikeCount())
                .replyCount(c.getReplyCount())
                .createdAt(str(c.getCreatedAt()))
                .updatedAt(str(c.getUpdatedAt()))
                .build();
    }

    private int orZero(Integer v)           { return v == null ? 0 : v; }
    private LocalDateTime parse(String s)   { return s == null ? null : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt)    { return dt == null ? null : dt.toString(); }
}
