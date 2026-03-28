package com.socialapp.infrastructure.comment.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Comment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CommentNode {

    @Id
    private String id;

    @Property("authorId")
    private String authorId;

    @Property("postId")
    private String postId;

    @Property("repliedToCommentId")
    private String repliedToCommentId;

    @Property("content")
    private String content;

    @Property("attachedFilePaths")
    private List<String> attachedFilePaths;

    @Property("likeCount")
    private Integer likeCount;

    @Property("replyCount")
    private Integer replyCount;

    @Property("createdAt")
    private String createdAt;

    @Property("updatedAt")
    private String updatedAt;
}