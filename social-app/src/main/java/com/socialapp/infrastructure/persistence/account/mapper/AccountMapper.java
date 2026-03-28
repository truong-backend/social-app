package com.socialapp.infrastructure.persistence.account.mapper;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.valueobject.HashedPassword;
import com.socialapp.domain.account.valueobject.Role;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.infrastructure.persistence.account.neo4j.AccountNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountMapper {

    public Account toDomain(AccountNode node) {
        VerifyCode verifyCode = null;
        if (node.getVerifyCode() != null) {
            verifyCode = VerifyCode.of(
                    node.getVerifyCode(),
                    Boolean.TRUE.equals(node.getVerifyCodeIsVerified()),
                    LocalDateTime.parse(node.getVerifyCodeExpiryTime())
            );
        }

        return Account.reconstitute(
                node.getId(),
                Email.of(node.getEmail()),
                HashedPassword.ofHashed(node.getPassword()),
                Role.valueOf(node.getRole()),
                Boolean.TRUE.equals(node.getIsVerified()),
                node.getUserId(),
                verifyCode
        );
    }

    public AccountNode toNode(Account account) {
        AccountNode node = AccountNode.builder()
                .id(account.getId())
                .email(account.getEmail().getValue())
                .password(account.getPassword().getValue())
                .role(account.getRole().name())
                .isVerified(account.isVerified())
                .userId(account.getUserId())
                .build();

        if (account.getVerifyCode() != null) {
            node.setVerifyCode(account.getVerifyCode().getCode());
            node.setVerifyCodeIsVerified(account.getVerifyCode().isVerified());
            node.setVerifyCodeExpiryTime(account.getVerifyCode().getExpiryTime().toString());
        }

        return node;
    }
}