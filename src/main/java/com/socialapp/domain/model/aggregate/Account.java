package com.socialapp.domain.model.aggregate;

import com.socialapp.domain.model.entity.VerifyCode;
import com.socialapp.domain.model.valueobject.Email;
import com.socialapp.domain.model.valueobject.HashedPassword;
import com.socialapp.domain.model.valueobject.UserRole;

/**
 * Aggregate Root: Account
 * ─────────────────────────────────────────────────────────────
 * Quản lý xác thực: đăng ký, đăng nhập, verify email, khóa TK.
 *
 * Rules:
 *   - Sai mật khẩu 5 lần → khóa 15 phút
 *   - Phải xác thực email trước khi đăng nhập
 *   - Mật khẩu lưu dạng bcrypt hash
 * ─────────────────────────────────────────────────────────────
 * Quan hệ trong graph:
 *   Account --HAS_INFO--> User
 *   Account --HAS_VERIFY_CODE--> VerifyCode
 */
public class Account {

    private static final int MAX_FAILED_ATTEMPTS  = 5;
    private static final int LOCK_DURATION_MILLIS = 15 * 60 * 1000;

    // ── Identity ─────────────────────────────────────────────
    private final String id;

    // ── Value Objects ─────────────────────────────────────────
    private Email          email;
    private HashedPassword password;
    private UserRole       role;

    // ── State ────────────────────────────────────────────────
    private boolean    isVerified;
    private int        failedAttempts;
    private Long       lockedUntilMs;   // null = không bị khóa

    // ── Child Entity ─────────────────────────────────────────
    private VerifyCode verifyCode;

    // ── Constructors ─────────────────────────────────────────

    /** Tạo mới */
    public Account(String id, Email email, HashedPassword password) {
        this.id             = id;
        this.email          = email;
        this.password       = password;
        this.role           = UserRole.USER;
        this.isVerified     = false;
        this.failedAttempts = 0;
    }

    /** Load từ DB */
    public Account(String id, Email email, HashedPassword password,
                   UserRole role, boolean isVerified,
                   int failedAttempts, Long lockedUntilMs) {
        this.id             = id;
        this.email          = email;
        this.password       = password;
        this.role           = role;
        this.isVerified     = isVerified;
        this.failedAttempts = failedAttempts;
        this.lockedUntilMs  = lockedUntilMs;
    }

    // ── Business Methods ─────────────────────────────────────

    public boolean isLocked() {
        if (lockedUntilMs == null) return false;
        if (System.currentTimeMillis() > lockedUntilMs) {
            lockedUntilMs  = null;
            failedAttempts = 0;
            return false;
        }
        return true;
    }

    /** Gọi sau khi compare bcrypt thất bại */
    public void recordFailedLogin() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntilMs  = System.currentTimeMillis() + LOCK_DURATION_MILLIS;
            failedAttempts = 0;
        }
    }

    public void resetFailedAttempts() { this.failedAttempts = 0; }

    public void setVerifyCode(VerifyCode code) {
        this.verifyCode = code;
    }

    /** Xác thực email qua code */
    public void verifyEmail(String inputCode) {
        if (verifyCode == null)
            throw new IllegalStateException("No verify code set for this account");
        verifyCode.verify(inputCode);   // ném exception nếu sai/hết hạn
        this.isVerified = true;
    }

    public void changePassword(HashedPassword newPassword) {
        this.password = newPassword;
    }

    // ── Getters ──────────────────────────────────────────────

    public String          getId()             { return id; }
    public Email           getEmail()          { return email; }
    public HashedPassword  getPassword()       { return password; }
    public UserRole        getRole()           { return role; }
    public boolean         isVerified()        { return isVerified; }
    public int             getFailedAttempts() { return failedAttempts; }
    public Long            getLockedUntilMs()  { return lockedUntilMs; }
    public VerifyCode      getVerifyCode()     { return verifyCode; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Account)) return false;
        return id.equals(((Account) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}