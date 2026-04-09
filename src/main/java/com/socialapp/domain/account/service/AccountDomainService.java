package com.socialapp.domain.account.service;

import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.exception.AccountDomainException;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDomainService {

    private static final long VERIFY_CODE_EXPIRY_MINUTES = 15;

    private final PasswordMatcher passwordMatcher;

    public AccountDomainService(PasswordMatcher passwordMatcher) {
        this.passwordMatcher = passwordMatcher;
    }

    public void validateLogin(Account account, String rawPassword) {
        account.validateCanLogin();
        boolean matches = passwordMatcher.matches(rawPassword, account.getPassword().getValue());
        if (!matches)
            throw new AccountDomainException("Invalid password");
    }

    /**
     * Kiểm tra password mà không throw exception — dùng cho delete account.
     */
    public boolean verifyPassword(Account account, String rawPassword) {
        return passwordMatcher.matches(rawPassword, account.getPassword().getValue());
    }

    public void validateRegister(Email email, boolean emailExists) {
        if (emailExists)
            throw new AccountDomainException("Email already in use: " + email.getValue());
    }

    public VerifyCode generateVerifyCode() {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(VERIFY_CODE_EXPIRY_MINUTES);
        return VerifyCode.of(code, false, expiry);
    }

    public interface PasswordMatcher {
        boolean matches(String rawPassword, String hashedPassword);
    }
}