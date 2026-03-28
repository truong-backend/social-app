package com.socialapp.infrastructure.persistence.comment.mapper;

import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.infrastructure.persistence.comment.neo4j.CommentNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CommentMapper {

    public Comment toDomain(CommentNode n) {
        return Comment.reconstitute(
                n.getId(), n.getAuthorId(), n.getPostId(),
                n.getRepliedToCommentId(), n.getContent(),
                n.getAttachedFilePaths() != null ? n.getAttachedFilePaths() : List.of(),
                orZero(n.getLikeCount()), orZero(n.getReplyCount()),
                parse(n.getCreatedAt()), parse(n.getUpdatedAt())
        );
    }

    public CommentNode toNode(Comment c) {
        return CommentNode.builder()
                .id(c.getId())
                .authorId(c.getAuthorId())
                .postId(c.getPostId())
                .repliedToCommentId(c.getRepliedToCommentId())
                .content(c.getContent())
                .attachedFilePaths(c.getAttachedFilePaths())
                .likeCount(c.getLikeCount())
                .replyCount(c.getReplyCount())
                .createdAt(str(c.getCreatedAt()))
                .updatedAt(str(c.getUpdatedAt()))
                .build();
    }

    private int orZero(Integer v) { return v == null ? 0 : v; }
    private LocalDateTime parse(String s) { return s == null ? null : LocalDateTime.parse(s); }
    private String str(LocalDateTime dt) { return dt == null ? null : dt.toString(); }
}
