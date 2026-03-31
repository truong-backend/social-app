package com.socialapp.infrastructure.persistence.account.mapper;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.valueobject.HashedPassword;
import com.socialapp.domain.account.valueobject.Role;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.infrastructure.persistence.account.neo4j.node.AccountNode;
import org.springframework.stereotype.Component;

/**
 * VerifyCode không còn được lưu trong AccountNode.
 * Việc load VerifyCode từ VerifyCodeNode được xử lý
 * hoàn toàn trong AccountRepositoryAdapter qua relationship HAS_VERIFY_CODE.
 */
@Component
public class AccountMapper {

    public Account toDomain(AccountNode node) {


        // verifyCode = null — AccountRepositoryAdapter sẽ tự resolve qua VerifyCodeNode nếu cần
        return Account.reconstitute(
                node.getId(),
                Email.of(node.getEmail()),
                HashedPassword.ofHashed(node.getPassword()),
                Role.valueOf(node.getRole()),
                Boolean.TRUE.equals(node.getIsVerified()),
                node.getUserId(),
                null
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
                .build();
    }
}
