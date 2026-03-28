package com.socialapp.infrastructure.post.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

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

    @Property("sharedFromPostId")
    private String sharedFromPostId;

    @Property("likeCount")
    private Integer likeCount;

    @Property("shareCount")
    private Integer shareCount;

    @Property("commentCount")
    private Integer commentCount;

    @Property("attachedFilePaths")
    private List<String> attachedFilePaths;

    @Property("keywords")
    private List<String> keywords;

    @Property("createdAt")
    private String createdAt;

    @Property("updatedAt")
    private String updatedAt;

    @Property("deletedAt")
    private String deletedAt;
}