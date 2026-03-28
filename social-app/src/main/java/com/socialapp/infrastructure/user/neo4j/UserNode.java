package com.socialapp.infrastructure.user.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

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
