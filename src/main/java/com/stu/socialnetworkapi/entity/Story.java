package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.UUID;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Story {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;

    String caption;
    String bgColor;     // chỉ dùng khi mediaType = "text"
    String mediaType;   // "image" | "video" | "text"
    int viewCount;

    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();

    ZonedDateTime deletedAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "POSTED_STORY", direction = Relationship.Direction.INCOMING)
    User author;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "STORY_MEDIA", direction = Relationship.Direction.OUTGOING)
    File mediaFile; // null nếu mediaType = "text"
}