package com.socialapp.domain.model.entity;

import java.time.LocalDateTime;

/**
 * Entity: VerifyCode
 * Identity: code (unique)
 * Thuộc Aggregate Account.
 *
 * Rules:
 *   - Hết hạn sau 15 phút
 *   - Chỉ dùng được 1 lần
 */
public class VerifyCode {

    private static final int EXPIRY_MINUTES = 15;

    private final String        code;
    private final LocalDateTime expiryTime;
    private       boolean       isVerified;

    public VerifyCode(String code) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Verify code cannot be blank");
        this.code       = code;
        this.expiryTime = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        this.isVerified = false;
    }

    /** Constructor dùng khi load từ DB */
    public VerifyCode(String code, LocalDateTime expiryTime, boolean isVerified) {
        this.code       = code;
        this.expiryTime = expiryTime;
        this.isVerified = isVerified;
    }

    public void verify(String inputCode) {
        if (isVerified)
            throw new IllegalStateException("Code already used");
        if (LocalDateTime.now().isAfter(expiryTime))
            throw new IllegalStateException("Verify code has expired");
        if (!this.code.equals(inputCode))
            throw new IllegalArgumentException("Invalid verify code");
        this.isVerified = true;
    }

    public boolean isExpired()    { return LocalDateTime.now().isAfter(expiryTime); }
    public boolean isVerified()   { return isVerified; }
    public String  getCode()      { return code; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
}