package com.socialapp.infrastructure.persistence.account.mapper;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.valueobject.HashedPassword;
import com.socialapp.domain.account.valueobject.Role;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.infrastructure.persistence.account.neo4j.node.AccountNode;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toDomain(AccountNode node) {
        return Account.reconstituteFull(
                node.getId(),
                Email.of(node.getEmail()),
                HashedPassword.ofHashed(node.getPassword()),
                Role.valueOf(node.getRole()),
                Boolean.TRUE.equals(node.getIsVerified()),
                node.getUserId(),
                null,                                         // verifyCode — resolve ở Adapter
                Boolean.TRUE.equals(node.getIsBanned()),
                node.getBanReason(),
                !Boolean.FALSE.equals(node.getIsActive())     // null → true (active by default)
        );
    }

    public AccountNode toNode(Account account) {
        return AccountNode.builder()
                .id(account.getId())
                .email(account.getEmail().getValue())
                .password(account.getPassword().getValue())
                .role(account.getRole().name())
                .isVerified(account.isVerified())
                .userId(account.getUserId())
                .isBanned(account.isBanned())
                .banReason(account.getBanReason())
                .isActive(account.isActive())
                .build();
    }
}