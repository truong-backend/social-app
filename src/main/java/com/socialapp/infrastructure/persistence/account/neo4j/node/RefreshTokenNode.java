package com.socialapp.infrastructure.persistence.account.neo4j.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Node("RefreshTokenNode")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshTokenNode {

    @Id
    private String id;
    private String accountId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean revoked;
}