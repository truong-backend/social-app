package com.socialapp.domain.user.entity;

import com.socialapp.domain.user.exception.UserDomainException;
import com.socialapp.domain.user.valueobject.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity / Aggregate Root: User
 *
 * Chịu trách nhiệm:
 *  - Quản lý thông tin cá nhân
 *  - Enforce giới hạn đổi tên / username / ngày sinh
 *  - Quản lý counter (friend, block, request)
 */
public class User {

    // ── Identity ──────────────────────────────────────────────
    private final String id;

    // ── Value Objects ─────────────────────────────────────────
    private Username username;
    private FullName fullName;
    private LocalDate birthdate;
    private String bio;

    // ── Counters ──────────────────────────────────────────────
    private UserCounts counts;

    // ── Change restrictions ───────────────────────────────────
    private ChangeRestriction changeRestriction;

    // ── Profile picture (file path reference) ─────────────────
    private String profilePicturePath;

    // ── Private constructor ───────────────────────────────────
    private User(String id, Username username, FullName fullName,
                 LocalDate birthdate, String bio,
                 UserCounts counts, ChangeRestriction changeRestriction,
                 String profilePicturePath) {
        this.id                = id;
        this.username          = username;
        this.fullName          = fullName;
        this.birthdate         = birthdate;
        this.bio               = bio;
        this.counts            = counts;
        this.changeRestriction = changeRestriction;
        this.profilePicturePath = profilePicturePath;
    }

    // ── Factory Methods ───────────────────────────────────────

    public static User create(Username username, FullName fullName, LocalDate birthdate) {
        return new User(
                UUID.randomUUID().toString(),
                username,
                fullName,
                birthdate,
                null,
                UserCounts.zero(),
                ChangeRestriction.noRestriction(),
                null
        );
    }

    public static User reconstitute(String id, Username username, FullName fullName,
                                    LocalDate birthdate, String bio,
                                    UserCounts counts, ChangeRestriction changeRestriction,
                                    String profilePicturePath) {
        return new User(id, username, fullName, birthdate, bio,
                counts, changeRestriction, profilePicturePath);
    }

    // ── Domain Behaviors ──────────────────────────────────────

    public void changeName(FullName newName) {
        if (!changeRestriction.canChangeName())
            throw new UserDomainException(
                    "Cannot change name until " + changeRestriction.getNextChangeNameDate());
        this.fullName          = newName;
        this.changeRestriction = changeRestriction.afterNameChanged();
    }

    public void changeUsername(Username newUsername) {
        if (!changeRestriction.canChangeUsername())
            throw new UserDomainException(
                    "Cannot change username until " + changeRestriction.getNextChangeUsernameDate());
        this.username          = newUsername;
        this.changeRestriction = changeRestriction.afterUsernameChanged();
    }

    public void changeBirthdate(LocalDate newDate) {
        if (!changeRestriction.canChangeBirthdate())
            throw new UserDomainException(
                    "Cannot change birthdate until " + changeRestriction.getNextChangeBirthdateDate());
        this.birthdate         = newDate;
        this.changeRestriction = changeRestriction.afterBirthdateChanged();
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void updateProfilePicture(String filePath) {
        this.profilePicturePath = filePath;
    }

    // ── Counter mutations (gọi từ relationship domain service) ─

    public void onFriendAdded()            { this.counts = counts.incrementFriend(); }
    public void onFriendRemoved()          { this.counts = counts.decrementFriend(); }
    public void onUserBlocked()            { this.counts = counts.incrementBlock(); }
    public void onUserUnblocked()          { this.counts = counts.decrementBlock(); }
    public void onRequestSent()            { this.counts = counts.incrementRequestSent(); }
    public void onRequestSentCancelled()   { this.counts = counts.decrementRequestSent(); }
    public void onRequestReceived()        { this.counts = counts.incrementRequestReceived(); }
    public void onRequestReceivedHandled() { this.counts = counts.decrementRequestReceived(); }

    // ── Getters ───────────────────────────────────────────────

    public String getId()                          { return id; }
    public Username getUsername()                  { return username; }
    public FullName getFullName()                  { return fullName; }
    public LocalDate getBirthdate()                { return birthdate; }
    public String getBio()                         { return bio; }
    public UserCounts getCounts()                  { return counts; }
    public ChangeRestriction getChangeRestriction(){ return changeRestriction; }
    public String getProfilePicturePath()          { return profilePicturePath; }
}