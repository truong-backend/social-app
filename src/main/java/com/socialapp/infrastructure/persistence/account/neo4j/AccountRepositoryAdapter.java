package com.socialapp.infrastructure.persistence.account.neo4j;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.infrastructure.persistence.account.mapper.AccountMapper;
import com.socialapp.infrastructure.persistence.account.neo4j.node.AccountNode;
import com.socialapp.infrastructure.persistence.account.neo4j.node.VerifyCodeNode;
import com.socialapp.infrastructure.persistence.account.neo4j.repository.AccountNeo4jRepository;
import com.socialapp.infrastructure.persistence.account.neo4j.repository.VerifyCodeNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountNeo4jRepository    neo4jRepository;
    private final VerifyCodeNeo4jRepository verifyCodeRepository;
    private final AccountMapper             mapper;

    @Override
    public Optional<Account> findByVerifyCode(String code) {
        return neo4jRepository.findByVerifyCode(code).map(node -> {
            Account account = mapper.toDomain(node);                    // verifyCode = null
            resolveAndInjectVerifyCode(account, node.getId());
            return account;
        });
    }

    @Override
    public Optional<Account> findById(String id) {
        return neo4jRepository.findById(id).map(node -> {
            Account account = mapper.toDomain(node);
            resolveAndInjectVerifyCode(account, id);
            return account;
        });
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return neo4jRepository.findByEmail(email.getValue()).map(node -> {
            Account account = mapper.toDomain(node);
            resolveAndInjectVerifyCode(account, node.getId());
            return account;
        });
    }

    @Override
    public boolean existsByEmail(Email email) {
        return neo4jRepository.existsByEmail(email.getValue());
    }

    @Override
    public Account save(Account account) {
        AccountNode saved = neo4jRepository.save(mapper.toNode(account));

        // (Account)-[:HAS_INFO]→(User)
        neo4jRepository.linkAccountToUser(saved.getId(), saved.getUserId());

        // (Account)-[:HAS_VERIFY_CODE]→(VerifyCode)
        if (account.getVerifyCode() != null) {
            VerifyCodeNode vcNode = VerifyCodeNode.builder()
                    .code(account.getVerifyCode().getCode())
                    .isVerified(account.getVerifyCode().isVerified())
                    .expiryTime(account.getVerifyCode().getExpiryTime().toString())
                    .build();
            verifyCodeRepository.save(vcNode);
            neo4jRepository.linkAccountToVerifyCode(saved.getId(), vcNode.getCode());
        }

        // Trả về domain với verifyCode đầy đủ
        Account result = mapper.toDomain(saved);
        if (account.getVerifyCode() != null) {
            result.assignVerifyCode(account.getVerifyCode());
        }
        return result;
    }

    @Override
    public void deleteById(String id) {
        neo4jRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return neo4jRepository.existsByEmail(email);
    }

    @Override
    public Optional<Account> findByUserId(String userId) {
        return neo4jRepository.findByUserId(userId).map(node -> {
            Account account = mapper.toDomain(node);
            resolveAndInjectVerifyCode(account, node.getId());
            return account;
        });
    }

    // ── Helper: resolve VerifyCode từ graph và inject vào Account ──

    private void resolveAndInjectVerifyCode(Account account, String accountId) {
        neo4jRepository.findVerifyCodeByAccountId(accountId)
                .ifPresent(vcNode -> account.assignVerifyCode(
                        VerifyCode.of(
                                vcNode.getCode(),
                                Boolean.TRUE.equals(vcNode.getIsVerified()),
                                LocalDateTime.parse(vcNode.getExpiryTime())
                        )
                ));
    }

}
