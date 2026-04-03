package com.socialapp.infrastructure.persistence.account.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Relationships (managed externally via AccountNeo4jRepository):
 *   (Account)-[:HAS_VERIFY_CODE]→(VerifyCode)
 *   (Account)-[:HAS_INFO]→(User)
 */
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
}
