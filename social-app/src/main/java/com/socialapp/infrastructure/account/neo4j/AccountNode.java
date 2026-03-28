package com.socialapp.infrastructure.account.neo4j;

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

    // VerifyCode được nhúng trực tiếp vào node (không cần separate node)
    @Property("verifyCode")
    private String verifyCode;

    @Property("verifyCodeIsVerified")
    private Boolean verifyCodeIsVerified;

    @Property("verifyCodeExpiryTime")
    private String verifyCodeExpiryTime;   // ISO string → parse ở mapper
}
