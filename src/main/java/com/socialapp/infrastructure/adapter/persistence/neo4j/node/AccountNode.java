package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Account")
public class AccountNode {

    @Id
    private String id;

    @Property("email")
    private String email;

    @Property("password")
    private String password;

    @Property("role")
    private String role;

    @Property("isVerified")
    private boolean verified;

    @Property("failedAttempts")
    private int failedAttempts;

    @Property("lockedUntilMs")
    private Long lockedUntilMs;

    @Relationship(type = "HAS_VERIFY_CODE", direction = Relationship.Direction.OUTGOING)
    private VerifyCodeNode verifyCode;

    @Relationship(type = "HAS_INFO", direction = Relationship.Direction.OUTGOING)
    private UserNode userInfo;

    // ===== Constructors =====

    public AccountNode() {
    }

    public AccountNode(String id, String email, String password,
                       String role, boolean verified,
                       int failedAttempts, Long lockedUntilMs) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verified = verified;
        this.failedAttempts = failedAttempts;
        this.lockedUntilMs = lockedUntilMs;
    }

    public AccountNode(String id, String email, String password,
                       String role, boolean verified,
                       int failedAttempts, Long lockedUntilMs,
                       VerifyCodeNode verifyCode,
                       UserNode userInfo) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verified = verified;
        this.failedAttempts = failedAttempts;
        this.lockedUntilMs = lockedUntilMs;
        this.verifyCode = verifyCode;
        this.userInfo = userInfo;
    }

    // ===== Getters / Setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Long getLockedUntilMs() {
        return lockedUntilMs;
    }

    public void setLockedUntilMs(Long lockedUntilMs) {
        this.lockedUntilMs = lockedUntilMs;
    }

    public VerifyCodeNode getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(VerifyCodeNode verifyCode) {
        this.verifyCode = verifyCode;
    }

    public UserNode getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserNode userInfo) {
        this.userInfo = userInfo;
    }
}