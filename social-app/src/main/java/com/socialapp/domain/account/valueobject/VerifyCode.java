package com.socialapp.domain.account.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object: VerifyCode
 * Bất biến (immutable). Chứa logic kiểm tra hết hạn và đã dùng.
 */
public final class VerifyCode {

    private final String code;
    private final boolean isVerified;
    private final LocalDateTime expiryTime;

    private VerifyCode(String code, boolean isVerified, LocalDateTime expiryTime) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("VerifyCode must not be blank");
        if (expiryTime == null)
            throw new IllegalArgumentException("ExpiryTime must not be null");
        this.code = code;
        this.isVerified = isVerified;
        this.expiryTime = expiryTime;
    }

    public static VerifyCode of(String code, boolean isVerified, LocalDateTime expiryTime) {
        return new VerifyCode(code, isVerified, expiryTime);
    }

    // ── Domain Logic ──────────────────────────────────────────

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isUsable() {
        return !isVerified && !isExpired();
    }

    public VerifyCode markAsVerified() {
        return new VerifyCode(this.code, true, this.expiryTime);
    }

    // ── Getters ───────────────────────────────────────────────

    public String getCode() { return code; }
    public boolean isVerified() { return isVerified; }
    public LocalDateTime getExpiryTime() { return expiryTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerifyCode vc)) return false;
        return Objects.equals(code, vc.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}