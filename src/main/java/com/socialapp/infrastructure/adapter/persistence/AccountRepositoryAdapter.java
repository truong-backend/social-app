package com.socialapp.infrastructure.adapter.persistence;

import com.socialapp.domain.model.aggregate.Account;
import com.socialapp.domain.model.entity.VerifyCode;
import com.socialapp.domain.model.valueobject.Email;
import com.socialapp.domain.model.valueobject.HashedPassword;
import com.socialapp.domain.model.valueobject.UserRole;
import com.socialapp.domain.repository.AccountRepository;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.AccountNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.VerifyCodeNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.AccountNeo4jRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountNeo4jRepository neo4jRepo;

    public AccountRepositoryAdapter(AccountNeo4jRepository neo4jRepo) {
        this.neo4jRepo = neo4jRepo;
    }

    // ── Domain → Node ────────────────────────────────────────

    private AccountNode toNode(Account account) {
        AccountNode node = new AccountNode(
                account.getId(),
                account.getEmail().getValue(),
                account.getPassword().getValue(),
                account.getRole().name(),
                account.isVerified(),
                account.getFailedAttempts(),
                account.getLockedUntilMs()
        );
        if (account.getVerifyCode() != null) {
            VerifyCode vc = account.getVerifyCode();
            // @Relationship HAS_VERIFY_CODE được lưu tự động khi set verifyCode
            node.setVerifyCode(new VerifyCodeNode(
                    vc.getCode(),
                    vc.isVerified(),
                    vc.getExpiryTime()
            ));
        }
        return node;
    }

    // ── Node → Domain ────────────────────────────────────────

    private Account toDomain(AccountNode node) {
        Account account = new Account(
                node.getId(),
                new Email(node.getEmail()),
                new HashedPassword(node.getPassword()),
                UserRole.valueOf(node.getRole()),
                node.isVerified(),
                node.getFailedAttempts(),
                node.getLockedUntilMs()
        );
        // @Relationship HAS_VERIFY_CODE → node.getVerifyCode() được SDN4j load tự động
        if (node.getVerifyCode() != null) {
            VerifyCodeNode vc = node.getVerifyCode();
            account.setVerifyCode(new VerifyCode(
                    vc.getCode(),
                    vc.getExpiryTime(),
                    vc.isVerified()
            ));
        }
        return account;
    }

    // ── Repository impl ──────────────────────────────────────

    @Override
    public Optional<Account> findById(String id) {
        return neo4jRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return neo4jRepo.findByEmail(email.getValue()).map(this::toDomain);
    }

    @Override
    public void save(Account account) {
        neo4jRepo.save(toNode(account));
    }

    @Override
    public void delete(String id) {
        neo4jRepo.deleteById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return neo4jRepo.existsByEmail(email.getValue());
    }
}