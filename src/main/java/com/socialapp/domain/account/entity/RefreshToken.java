package com.socialapp.domain.account.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshToken {

    private final String        id;
    private final String        accountId;
    private final String        token;
    private final LocalDateTime expiresAt;
    private       boolean       revoked;

    private RefreshToken(String id, String accountId, String token,
                         LocalDateTime expiresAt, boolean revoked) {
        this.id        = id;
        this.accountId = accountId;
        this.token     = token;
        this.expiresAt = expiresAt;
        this.revoked   = revoked;
    }

    public static RefreshToken create(String accountId, int expiryDays) {
        return new RefreshToken(
                UUID.randomUUID().toString(),
                accountId,
                UUID.randomUUID().toString().replace("-", "") +
                        UUID.randomUUID().toString().replace("-", ""),
                LocalDateTime.now().plusDays(expiryDays),
                false
        );
    }

    public static RefreshToken reconstitute(String id, String accountId, String token,
                                            LocalDateTime expiresAt, boolean revoked) {
        return new RefreshToken(id, accountId, token, expiresAt, revoked);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    public String        getId()        { return id; }
    public String        getAccountId() { return accountId; }
    public String        getToken()     { return token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean       isRevoked()    { return revoked; }
}