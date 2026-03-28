package com.socialapp.infrastructure.persistence.message.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

@Node("Message")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MessageNode {

    @Id
    private String id;

    @Property("senderId")
    private String senderId;

    @Property("chatId")
    private String chatId;

    @Property("content")
    private String content;

    @Property("attachedFilePaths")
    private List<String> attachedFilePaths;

    @Property("isRead")
    private Boolean isRead;

    @Property("deletedForEveryoneAt")
    private String deletedForEveryoneAt;

    @Property("deletedForSenderAt")
    private String deletedForSenderAt;

    @Property("sentAt")
    private String sentAt;

    @Property("updatedAt")
    private String updatedAt;
}
