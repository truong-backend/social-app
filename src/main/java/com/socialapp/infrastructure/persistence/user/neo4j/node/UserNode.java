package com.socialapp.infrastructure.persistence.user.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via UserNeo4jRepository / RelationshipNeo4jRepository):
 *   (User)-[:HAS_PROFILE_PICTURE]→(File)
 *   (User)-[:UPLOAD_FILE]→(File)
 *   (User)-[:FRIEND {createdAt}]-(User)
 *   (User)-[:BLOCK]→(User)
 *   (User)-[:REQUEST {sentAt}]→(User)
 *   (User)-[:POSTED]→(Post)
 *   (User)-[:LIKED]→(Post)
 *   (User)-[:LIKED]→(Comment)
 *   (User)-[:COMMENTED]→(Comment)
 *   (User)-[:INTERACT_WITH]→(Keyword)
 *   (User)-[:HAS_NOTIFICATION]→(Notification)
 *   (User)-[:IS_MEMBER_OF]→(Chat)
 *   (User)-[:SENT]→(Message)
 */
@Node("User")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserNode {

    @Id
    private String id;

    @Property("username")
    private String username;

    @Property("familyName")
    private String familyName;

    @Property("givenName")
    private String givenName;

    @Property("birthdate")
    private String birthdate;         // "yyyy-MM-dd"

    @Property("bio")
    private String bio;

    @Property("friendCount")
    private Integer friendCount;

    @Property("blockCount")
    private Integer blockCount;

    @Property("requestSentCount")
    private Integer requestSentCount;

    @Property("requestReceivedCount")
    private Integer requestReceivedCount;

    @Property("nextChangeNameDate")
    private String nextChangeNameDate;

    @Property("nextChangeUsernameDate")
    private String nextChangeUsernameDate;

    @Property("nextChangeBirthdateDate")
    private String nextChangeBirthdateDate;

    @Property("profilePicturePath")
    private String profilePicturePath;
}
