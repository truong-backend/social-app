package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.GroupRole;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Chat {
    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    UUID id;

    @Builder.Default
    ZonedDateTime createdAt = ZonedDateTime.now();

    // null = direct chat, non-null = group chat
    String groupName;
    String groupAvatarFileId;
    Boolean isGroup;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "IS_MEMBER_OF", direction = Relationship.Direction.INCOMING)
    List<User> members;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "HAS_MESSAGE", direction = Relationship.Direction.OUTGOING)
    List<Message> messages;

    // Pinned messages (up to 3)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Relationship(type = "PINNED_MESSAGE", direction = Relationship.Direction.OUTGOING)
    List<Message> pinnedMessages;
}