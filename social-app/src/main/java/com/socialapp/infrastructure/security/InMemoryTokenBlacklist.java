package com.socialapp.infrastructure.security;

import com.socialapp.application.account.usecase.logout.LogoutUseCase.TokenBlacklist;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory blacklist.
 * Production: thay bằng Redis với TTL = token expiry.
 */
@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Set<String> revokedTokens =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void revoke(String token) {
        revokedTokens.add(token);
    }

    @Override
    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
