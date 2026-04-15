package com.socialapp.domain.repository;

import com.socialapp.domain.model.aggregate.Account;
import com.socialapp.domain.model.valueobject.Email;

import java.util.Optional;

/**
 * Repository: Account
 * Infrastructure (Neo4j adapter) sẽ implement interface này.
 */
    public interface AccountRepository {

    Optional<Account> findById(String id);

    Optional<Account> findByEmail(Email email);

    void save(Account account);

    void delete(String id);

    boolean existsByEmail(Email email);
}