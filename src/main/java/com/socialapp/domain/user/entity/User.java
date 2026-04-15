package com.socialapp.domain.user.entity;

import com.socialapp.domain.user.exception.UserDomainException;
import com.socialapp.domain.user.valueobject.*;

import java.time.LocalDate;
import java.util.UUID;

public class User {

    private final String id;
    private Username username;
    private FullName fullName;
    private LocalDate birthdate;
    private String bio;
    private UserCounts counts;
    private ChangeRestriction changeRestriction;
    private String profilePicturePath;
    private boolean isDeleted;

    private User(String id, Username username, FullName fullName,
                 LocalDate birthdate, String bio,
                 UserCounts counts, ChangeRestriction changeRestriction,
                 String profilePicturePath) {
        this.id                 = id;
        this.username           = username;
        this.fullName           = fullName;
        this.birthdate          = birthdate;
        this.bio                = bio;
        this.counts             = counts;
        this.changeRestriction  = changeRestriction;
        this.profilePicturePath = profilePicturePath;
    }

    public static User create(Username username, FullName fullName, LocalDate birthdate) {
        return new User(
                UUID.randomUUID().toString(),
                username, fullName, birthdate, null,
                UserCounts.zero(), ChangeRestriction.noRestriction(), null
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

    /**
     * Xóa thông tin cá nhân khi user yêu cầu xóa tài khoản.
     * Giữ lại id để các bài post / comment vẫn tham chiếu được.
     */
    public void anonymize() {
        this.username           = new Username("deleted_" + id.substring(0, 8));
        this.fullName           = FullName.of("Deleted", "User");
        this.bio                = null;
        this.birthdate          = null;
        this.profilePicturePath = null;
        this.isDeleted          = true;
    }

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

    public void onFriendAdded()            { this.counts = counts.incrementFriend(); }
    public void onFriendRemoved()          { this.counts = counts.decrementFriend(); }
    public void onUserBlocked()            { this.counts = counts.incrementBlock(); }
    public void onUserUnblocked()          { this.counts = counts.decrementBlock(); }
    public void onRequestSent()            { this.counts = counts.incrementRequestSent(); }
    public void onRequestSentCancelled()   { this.counts = counts.decrementRequestSent(); }
    public void onRequestReceived()        { this.counts = counts.incrementRequestReceived(); }
    public void onRequestReceivedHandled() { this.counts = counts.decrementRequestReceived(); }

    // ── Getters ───────────────────────────────────────────────
    public String getId()                           { return id; }
    public Username getUsername()                   { return username; }
    public FullName getFullName()                   { return fullName; }
    public LocalDate getBirthdate()                 { return birthdate; }
    public String getBio()                          { return bio; }
    public UserCounts getCounts()                   { return counts; }
    public ChangeRestriction getChangeRestriction() { return changeRestriction; }
    public String getProfilePicturePath()           { return profilePicturePath; }
    public boolean isDeleted()                      { return isDeleted; }
}