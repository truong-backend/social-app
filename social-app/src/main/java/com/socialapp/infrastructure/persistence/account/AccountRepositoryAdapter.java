package com.socialapp.infrastructure.persistence.account;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.infrastructure.persistence.account.mapper.AccountMapper;
import com.socialapp.infrastructure.persistence.account.neo4j.AccountNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountNeo4jRepository neo4jRepository;
    private final AccountMapper          mapper;

    @Override
    public Optional<Account> findById(String id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return neo4jRepository.findByEmail(email.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return neo4jRepository.existsByEmail(email.getValue());
    }

    @Override
    public Account save(Account account) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(account)));
    }

    @Override
    public void deleteById(String id) {
        neo4jRepository.deleteById(id);
    }
}