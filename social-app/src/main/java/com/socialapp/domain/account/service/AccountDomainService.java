package com.socialapp.domain.account.service;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.exception.AccountDomainException;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Service: AccountDomainService
 *
 * Chứa business logic không thuộc về một entity cụ thể:
 *  - Validate đăng nhập (so password hash)
 *  - Tạo VerifyCode mới
 *  - Validate đăng ký (email chưa tồn tại)
 */
public class AccountDomainService {

    private static final long VERIFY_CODE_EXPIRY_MINUTES = 15;

    // ── Password matching (injected as strategy) ──────────────

    private final PasswordMatcher passwordMatcher;

    public AccountDomainService(PasswordMatcher passwordMatcher) {
        this.passwordMatcher = passwordMatcher;
    }

    /**
     * Validate thông tin đăng nhập
     */
    public void validateLogin(Account account, String rawPassword) {
        account.validateCanLogin();
        boolean matches = passwordMatcher.matches(rawPassword, account.getPassword().getValue());
        if (!matches)
            throw new AccountDomainException("Invalid password");
    }

    /**
     * Validate đăng ký: email chưa được dùng
     */
    public void validateRegister(Email email, boolean emailExists) {
        if (emailExists)
            throw new AccountDomainException("Email already in use: " + email.getValue());
    }

    /**
     * Tạo VerifyCode mới để gán cho Account
     */
    public VerifyCode generateVerifyCode() {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(VERIFY_CODE_EXPIRY_MINUTES);
        return VerifyCode.of(code, false, expiry);
    }

    /**
     * Port interface: PasswordMatcher
     * Được implement ở infrastructure (BCryptPasswordEncoder)
     */
    public interface PasswordMatcher {
        boolean matches(String rawPassword, String hashedPassword);
    }
}
