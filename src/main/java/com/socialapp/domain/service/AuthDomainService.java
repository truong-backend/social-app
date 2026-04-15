package com.socialapp.domain.service;

import com.socialapp.application.dto.response.ErrorCode;
import com.socialapp.domain.model.aggregate.Account;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.VerifyCode;
import com.socialapp.domain.model.valueobject.*;
import com.socialapp.domain.repository.AccountRepository;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.presentation.advice.DomainException;

import java.util.UUID;

/**
 * Domain Service: AuthDomainService
 * ─────────────────────────────────────────────────────────────
 * Xử lý nghiệp vụ xác thực — logic nằm giữa nhiều Aggregate.
 *
 * Trách nhiệm:
 *   - Tạo Account + User khi đăng ký
 *   - Kiểm tra đăng nhập (hợp lệ, bị khóa, chưa verify)
 *   - Phát sinh và gán VerifyCode
 *   - Đổi mật khẩu
 * ─────────────────────────────────────────────────────────────
 * Lưu ý: Domain Service KHÔNG inject Spring bean,
 * chỉ inject Repository interface và Port interface.
 */
public class AuthDomainService {

    private final AccountRepository accountRepository;
    private final UserRepository    userRepository;
    private final PasswordHasher    passwordHasher;

    public AuthDomainService(AccountRepository accountRepository,
                             UserRepository userRepository,
                             PasswordHasher passwordHasher) {
        this.accountRepository = accountRepository;
        this.userRepository    = userRepository;
        this.passwordHasher    = passwordHasher;
    }

    // ── Register ─────────────────────────────────────────────

    public Account register(Email email, RawPassword rawPassword,
                            String familyName, String givenName,
                            Birthdate birthdate) {

        if (accountRepository.existsByEmail(email))
            throw new DomainException(ErrorCode.USERNAME_TAKEN,
                    "Email already registered: " + email.getValue());

        String accountId = UUID.randomUUID().toString();
        HashedPassword hashed = passwordHasher.hash(rawPassword);
        Account account = new Account(accountId, email, hashed);

        VerifyCode code = new VerifyCode(UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        account.setVerifyCode(code);

        String rawUsername = accountId.replace("-", "").substring(0, 32);
        Username username  = new Username(rawUsername);

        User user = new User(new UserId(accountId), familyName, givenName, birthdate, username);

        accountRepository.save(account);
        userRepository.save(user);

        return account;
    }

    // ── Login ─────────────────────────────────────────────────

    public Account login(Email email, RawPassword rawPassword) {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVALID_CREDENTIALS, "Account not found"));

        if (account.isLocked())
            throw new DomainException(ErrorCode.ACCOUNT_LOCKED,
                    "Account is locked. Please try again in 15 minutes.");

        if (!account.isVerified())
            throw new DomainException(ErrorCode.ACCOUNT_NOT_VERIFIED,
                    "Email is not verified. Please verify your email.");

        boolean match = passwordHasher.matches(rawPassword, account.getPassword());
        if (!match) {
            account.recordFailedLogin();
            accountRepository.save(account);
            throw new DomainException(ErrorCode.INVALID_CREDENTIALS, "Invalid password");
        }

        account.resetFailedAttempts();
        accountRepository.save(account);
        return account;
    }

    // ── Verify Email ─────────────────────────────────────────

    public void verifyEmail(String accountId, String inputCode) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.USER_NOT_FOUND, "Account not found"));
        account.verifyEmail(inputCode);
        accountRepository.save(account);
    }

    // ── Change Password ───────────────────────────────────────

    public void changePassword(String accountId, RawPassword oldRaw, RawPassword newRaw) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.USER_NOT_FOUND, "Account not found"));

        if (!passwordHasher.matches(oldRaw, account.getPassword()))
            throw new DomainException(ErrorCode.INVALID_CREDENTIALS,
                    "Current password is incorrect");

        account.changePassword(passwordHasher.hash(newRaw));
        accountRepository.save(account);
    }

    // ── Resend Verify Code ────────────────────────────────────

    public VerifyCode resendVerifyCode(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.USER_NOT_FOUND, "Account not found"));

        if (account.isVerified())
            throw new DomainException(ErrorCode.EMAIL_ALREADY_VERIFIED,
                    "Account already verified");

        VerifyCode newCode = new VerifyCode(
                UUID.randomUUID().toString().replace("-", "").substring(0, 6));
        account.setVerifyCode(newCode);
        accountRepository.save(account);
        return newCode;
    }
}