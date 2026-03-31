package com.socialapp.infrastructure.persistence.post.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via PostNeo4jRepository):
 *   (User)-[:POSTED]→(Post)
 *   (User)-[:LIKED]→(Post)
 *   (Post)-[:SHARE]→(Post)
 *   (Post)-[:ATTACH_FILE]→(File)
 *   (Post)-[:HAS_COMMENT]→(Comment)
 *   (Post)-[:HAS_KEYWORD]→(Keyword)
 */
@Node("Post")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostNode {

    @Id
    private String id;

    @Property("authorId")
    private String authorId;

    @Property("content")
    private String content;

    @Property("privacy")
    private String privacy;

    @Property("likeCount")
    private Integer likeCount;

    @Property("shareCount")
    private Integer shareCount;

    @Property("commentCount")
    private Integer commentCount;

    @Property("createdAt")
    private String createdAt;

    @Property("updatedAt")
    private String updatedAt;

    @Property("deletedAt")
    private String deletedAt;
}
