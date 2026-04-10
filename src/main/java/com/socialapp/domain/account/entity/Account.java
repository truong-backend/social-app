package com.socialapp.domain.account.entity;

import com.socialapp.domain.account.exception.AccountDomainException;
import com.socialapp.domain.account.valueobject.HashedPassword;
import com.socialapp.domain.account.valueobject.Role;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;

import java.util.UUID;

/**
 * Entity / Aggregate Root: Account
 * <p>
 * Chịu trách nhiệm:
 * - Quản lý thông tin xác thực (email, password, role)
 * - Quản lý vòng đời VerifyCode
 * - Enforce business rules: xác thực email trước khi đăng nhập
 */
public class Account {

    // ── Identity ──────────────────────────────────────────────
    private final String id;

    // ── Value Objects ─────────────────────────────────────────
    private Email email;
    private HashedPassword password;
    private Role role;
    private boolean isVerified;

    // ── Owned Value Object (VerifyCode) ───────────────────────
    private VerifyCode verifyCode;

    // ── Linked User id (reference, not full object) ───────────
    private String userId;

    private boolean isBanned;
    private String banReason;
    private boolean isActive = true;

    // ── Private constructor (dùng factory method) ─────────────
    private Account(String id, Email email, HashedPassword password,
                    Role role, boolean isVerified, String userId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
        this.userId = userId;
    }

    // ── Factory Methods ───────────────────────────────────────

    public void changeEmail(String newEmailStr) {
        this.email = new Email(newEmailStr); // tạo value object Email
        this.isVerified = false; // cần xác thực lại
    }
    /**
     * Tạo Account mới (đăng ký lần đầu)
     */
    public static Account create(Email email, HashedPassword password, String userId) {
        return new Account(
                UUID.randomUUID().toString(),
                email,
                password,
                Role.USER,
                false,
                userId
        );
    }

    /**
     * Reconstitute từ persistence layer
     */
    public static Account reconstitute(String id, Email email, HashedPassword password,
                                       Role role, boolean isVerified,
                                       String userId, VerifyCode verifyCode) {
        Account account = new Account(id, email, password, role, isVerified, userId);
        account.verifyCode = verifyCode;
        return account;
    }

    // Thêm method reconstituteFull vào Account.java:
    public static Account reconstituteFull(String id, Email email, HashedPassword password,
                                           Role role, boolean isVerified,
                                           String userId, VerifyCode verifyCode,
                                           boolean isBanned, String banReason, boolean isActive) {
        Account account = new Account(id, email, password, role, isVerified, userId);
        account.verifyCode  = verifyCode;
        account.isBanned    = isBanned;
        account.banReason   = banReason;
        account.isActive    = isActive;
        return account;
    }

    // ── Domain Behaviors ──────────────────────────────────────

    /**
     * Gán VerifyCode mới (khi đăng ký hoặc đổi mật khẩu)
     */
    public void assignVerifyCode(VerifyCode code) {
        this.verifyCode = code;
    }

    /**
     * Xác nhận email bằng code
     */
    public void confirmEmail(String inputCode) {
        if (verifyCode == null)
            throw new AccountDomainException("No verify code assigned");
        if (!verifyCode.getCode().equals(inputCode))
            throw new AccountDomainException("Verify code does not match");
        if (!verifyCode.isUsable())
            throw new AccountDomainException("Verify code is expired or already used");

        this.verifyCode = verifyCode.markAsVerified();
        this.isVerified = true;
    }

    /**
     * Kiểm tra đăng nhập (raw password so với hash — logic match do domain service xử lý)
     */
    public void validateCanLogin() {
        if (!isVerified)
            throw new AccountDomainException("Email has not been verified");
    }

    /**
     * Đổi mật khẩu (sau khi verify code đã xác nhận)
     */
    public void changePassword(HashedPassword newPassword) {
        if (!isVerified)
            throw new AccountDomainException("Account must be verified before changing password");
        this.password = newPassword;
    }



    // ── Getters (không có setters — bất biến từ ngoài) ────────

    public void ban(String reason) {
        this.isBanned = true;
        this.banReason = reason;
    }

    public void unban() {
        this.isBanned = false;
        this.banReason = null;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public String getBanReason() {
        return banReason;
    }

    public String getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public VerifyCode getVerifyCode() {
        return verifyCode;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isActive() {
        return isActive;
    }
}
