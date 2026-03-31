package com.socialapp.infrastructure.persistence.comment.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via CommentNeo4jRepository):
 *   (User)-[:COMMENTED]→(Comment)
 *   (Post)-[:HAS_COMMENT]→(Comment)
 *   (Comment)-[:REPLIED]→(Comment)
 *   (User)-[:LIKED]→(Comment)
 *   (Comment)-[:ATTACH_FILE]→(File)
 */
@Node("Comment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CommentNode {

    @Id
    private String id;

    @Property("content")
    private String content;

    @Property("likeCount")
    private Integer likeCount;

    @Property("replyCount")
    private Integer replyCount;

    @Property("createdAt")
    private String createdAt;

    @Property("updatedAt")
    private String updatedAt;
}
