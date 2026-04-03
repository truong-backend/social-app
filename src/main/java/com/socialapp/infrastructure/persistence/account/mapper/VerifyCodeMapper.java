package com.socialapp.infrastructure.persistence.account.mapper;

import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.infrastructure.persistence.account.neo4j.node.VerifyCodeNode;

import java.time.LocalDateTime;

public class VerifyCodeMapper {

    // Node -> Domain
    public static VerifyCode toDomain(VerifyCodeNode node) {
        if (node == null) return null;

        return new VerifyCode(
                node.getCode(),
                Boolean.TRUE.equals(node.getIsVerified()),
                LocalDateTime.parse(node.getExpiryTime())
        );
    }

    // Domain -> Node
    public static VerifyCodeNode toNode(VerifyCode domain) {
        if (domain == null) return null;

        return VerifyCodeNode.builder()
                .code(domain.getCode())
                .isVerified(domain.isVerified())
                .expiryTime(domain.getExpiryTime().toString())
                .build();
    }
}