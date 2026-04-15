package com.socialapp.domain.model.aggregate;

import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Notification;
import com.socialapp.domain.model.valueobject.Birthdate;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.model.valueobject.Username;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: User
 * ─────────────────────────────────────────────────────────────
 * Quản lý hồ sơ cá nhân + mạng xã hội (friends, blocks, requests).
 *
 * Rules:
 *   - Mỗi lần đổi tên/ngày sinh/username → phải chờ 60 ngày
 *   - Tối đa 100 bạn bè, 100 block, 100 lời mời gửi/nhận
 *   - Tuổi ≥ 16 (validate trong Birthdate VO)
 * ─────────────────────────────────────────────────────────────
 * Quan hệ trong graph:
 *   User --FRIEND--> User
 *   User --BLOCK--> User
 *   User --REQUEST--> User
 *   User --HAS_NOTIFICATION--> Notification
 *   User --HAS_PROFILE_PICTURE--> File
 *   User --UPLOAD_FILE--> File
 */
public class User {

    private static final int MAX_FRIENDS          = 100;
    private static final int MAX_BLOCKS           = 100;
    private static final int MAX_SENT_REQUESTS    = 100;
    private static final int MAX_RECEIVED_REQUESTS= 100;
    private static final int CHANGE_COOLDOWN_DAYS = 60;

    // ── Identity ─────────────────────────────────────────────
    private final UserId id;

    // ── Value Objects ─────────────────────────────────────────
    private String     familyName;
    private String     givenName;
    private Birthdate  birthdate;
    private Username   username;
    private String     bio;

    // ── Counters ─────────────────────────────────────────────
    private int friendCount;
    private int requestSentCount;
    private int requestReceivedCount;
    private int blockCount;

    // ── Cooldown dates ────────────────────────────────────────
    private LocalDate nextChangeNameDate;
    private LocalDate nextChangeBirthdateDate;
    private LocalDate nextChangeUsernameDate;

    // ── Child entities ────────────────────────────────────────
    private FileEntity           profilePicture;
    private final List<Notification> notifications = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────

    /** Tạo mới */
    public User(UserId id, String familyName, String givenName,
                Birthdate birthdate, Username username) {
        this.id                      = id;
        this.familyName              = familyName;
        this.givenName               = givenName;
        this.birthdate               = birthdate;
        this.username                = username;
        this.friendCount             = 0;
        this.requestSentCount        = 0;
        this.requestReceivedCount    = 0;
        this.blockCount              = 0;
        LocalDate epoch              = LocalDate.of(2000, 1, 1);
        this.nextChangeNameDate      = epoch;
        this.nextChangeBirthdateDate = epoch;
        this.nextChangeUsernameDate  = epoch;
    }

    // ── Profile edits (60-day cooldown) ──────────────────────

    public void changeName(String familyName, String givenName) {
        if (LocalDate.now().isBefore(nextChangeNameDate))
            throw new IllegalStateException(
                    "Name can only be changed after " + nextChangeNameDate);
        this.familyName         = familyName;
        this.givenName          = givenName;
        this.nextChangeNameDate = LocalDate.now().plusDays(CHANGE_COOLDOWN_DAYS);
    }

    public void changeBirthdate(Birthdate birthdate) {
        if (LocalDate.now().isBefore(nextChangeBirthdateDate))
            throw new IllegalStateException(
                    "Birthdate can only be changed after " + nextChangeBirthdateDate);
        this.birthdate               = birthdate;
        this.nextChangeBirthdateDate = LocalDate.now().plusDays(CHANGE_COOLDOWN_DAYS);
    }

    public void changeUsername(Username username) {
        if (LocalDate.now().isBefore(nextChangeUsernameDate))
            throw new IllegalStateException(
                    "Username can only be changed after " + nextChangeUsernameDate);
        this.username               = username;
        this.nextChangeUsernameDate = LocalDate.now().plusDays(CHANGE_COOLDOWN_DAYS);
    }

    public void updateBio(String bio) { this.bio = bio; }

    public void setProfilePicture(FileEntity file) {
        this.profilePicture = file;
    }

    // ── Social graph ─────────────────────────────────────────

    public void sendFriendRequest() {
        if (requestSentCount >= MAX_SENT_REQUESTS)
            throw new IllegalStateException("Maximum sent friend requests (100) reached");
        requestSentCount++;
    }

    public void cancelSentRequest() {
        if (requestSentCount > 0) requestSentCount--;
    }

    public void receiveRequest() {
        if (requestReceivedCount >= MAX_RECEIVED_REQUESTS)
            throw new IllegalStateException("Maximum received friend requests (100) reached");
        requestReceivedCount++;
    }

    public void cancelReceivedRequest() {
        if (requestReceivedCount > 0) requestReceivedCount--;
    }

    public void addFriend() {
        if (friendCount >= MAX_FRIENDS)
            throw new IllegalStateException("Maximum friends (100) reached");
        friendCount++;
    }

    public void removeFriend() {
        if (friendCount > 0) friendCount--;
    }

    public void blockUser() {
        if (blockCount >= MAX_BLOCKS)
            throw new IllegalStateException("Maximum blocked users (100) reached");
        blockCount++;
    }

    public void unblockUser() {
        if (blockCount > 0) blockCount--;
    }

    // ── Notifications ─────────────────────────────────────────

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> getUnreadNotifications() {
        return notifications.stream()
                .filter(n -> !n.isRead())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Notification> getAllNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    // ── Getters ──────────────────────────────────────────────

    public UserId     getId()                      { return id; }
    public String     getFamilyName()              { return familyName; }
    public String     getGivenName()               { return givenName; }
    public Birthdate  getBirthdate()               { return birthdate; }
    public Username   getUsername()                { return username; }
    public String     getBio()                     { return bio; }
    public int        getFriendCount()             { return friendCount; }
    public int        getRequestSentCount()        { return requestSentCount; }
    public int        getRequestReceivedCount()    { return requestReceivedCount; }
    public int        getBlockCount()              { return blockCount; }
    public LocalDate  getNextChangeNameDate()      { return nextChangeNameDate; }
    public LocalDate  getNextChangeBirthdateDate() { return nextChangeBirthdateDate; }
    public LocalDate  getNextChangeUsernameDate()  { return nextChangeUsernameDate; }
    public FileEntity getProfilePicture()          { return profilePicture; }

    // Setters chỉ dùng khi load từ DB (package-private hoặc dùng builder)
    public void setFriendCount(int v)             { this.friendCount = v; }
    public void setRequestSentCount(int v)        { this.requestSentCount = v; }
    public void setRequestReceivedCount(int v)    { this.requestReceivedCount = v; }
    public void setBlockCount(int v)              { this.blockCount = v; }
    public void setNextChangeNameDate(LocalDate d)      { this.nextChangeNameDate = d; }
    public void setNextChangeBirthdateDate(LocalDate d) { this.nextChangeBirthdateDate = d; }
    public void setNextChangeUsernameDate(LocalDate d)  { this.nextChangeUsernameDate = d; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        return id.equals(((User) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
}