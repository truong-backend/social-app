package com.socialapp.infrastructure.persistence.account.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("Account")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AccountNode {

    @Id
    private String id;

    @Property("email")
    private String email;

    @Property("password")
    private String password;

    @Property("role")
    private String role;

    @Property("isVerified")
    private Boolean isVerified;

    @Property("userId")
    private String userId;

    @Property("isBanned")
    private Boolean isBanned;

    @Property("banReason")
    private String banReason;

    @Property("isActive")
    private Boolean isActive;
}