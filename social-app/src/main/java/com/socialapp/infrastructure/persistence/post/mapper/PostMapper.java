package com.socialapp.infrastructure.persistence.post.mapper;

import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.valueobject.PostCounts;
import com.socialapp.domain.post.valueobject.Privacy;
import com.socialapp.infrastructure.persistence.post.neo4j.PostNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostMapper {

    public Post toDomain(PostNode n) {
        return Post.reconstitute(
                n.getId(), n.getAuthorId(), n.getContent(),
                Privacy.valueOf(n.getPrivacy()),
                n.getSharedFromPostId(),
                n.getAttachedFilePaths() != null ? n.getAttachedFilePaths() : List.of(),
                n.getKeywords() != null ? n.getKeywords() : List.of(),
                PostCounts.of(
                        orZero(n.getLikeCount()),
                        orZero(n.getShareCount()),
                        orZero(n.getCommentCount())
                ),
                parseDateTime(n.getCreatedAt()),
                parseDateTime(n.getUpdatedAt()),
                parseDateTime(n.getDeletedAt())
        );
    }

    public PostNode toNode(Post p) {
        return PostNode.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
                .content(p.getContent())
                .privacy(p.getPrivacy().name())
                .sharedFromPostId(p.getSharedFromPostId())
                .likeCount(p.getCounts().getLikeCount())
                .shareCount(p.getCounts().getShareCount())
                .commentCount(p.getCounts().getCommentCount())
                .attachedFilePaths(p.getAttachedFilePaths())
                .keywords(p.getKeywords())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                .deletedAt(p.getDeletedAt() != null ? p.getDeletedAt().toString() : null)
                .build();
    }

    private int orZero(Integer v) { return v == null ? 0 : v; }

    private LocalDateTime parseDateTime(String s) {
        return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s);
    }
}
