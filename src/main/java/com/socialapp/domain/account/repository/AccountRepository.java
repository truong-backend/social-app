package com.socialapp.domain.account.repository;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.shared.valueobject.Email;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByVerifyCode(String verifyCode);
    Optional<Account> findById(String id);
    Optional<Account> findByEmail(Email email);
    Optional<Account> findByUserId(String userId);

    boolean existsByEmail(Email email);

    Account save(Account account);
    void deleteById(String id);
    boolean existsByEmail(String email);
}