package com.socialapp.domain.account.repository;

import com.socialapp.domain.account.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByAccountId(String accountId);
    void deleteExpired();
}