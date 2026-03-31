package com.socialapp.infrastructure.persistence.account.neo4j.node;

import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

@Node("VerifyCode")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyCodeNode {

    @Id
    private String code;

    @Property("isVerified")
    private Boolean isVerified;

    @Property("expiryTime")
    private String expiryTime;   // ISO string → parse ở mapper

}
