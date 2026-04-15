package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Node("User")
public class UserNode {

    @Id
    private String id;

    @Property("username")
    private String username;

    @Property("familyName")
    private String familyName;

    @Property("givenName")
    private String givenName;

    @Property("birthdate")
    private LocalDate birthdate;

    @Property("bio")
    private String bio;

    @Property("friendCount")
    private int friendCount;

    @Property("requestSentCount")
    private int requestSentCount;

    @Property("requestReceivedCount")
    private int requestReceivedCount;

    @Property("blockCount")
    private int blockCount;

    @Property("nextChangeNameDate")
    private LocalDate nextChangeNameDate;

    @Property("nextChangeBirthdateDate")
    private LocalDate nextChangeBirthdateDate;

    @Property("nextChangeUsernameDate")
    private LocalDate nextChangeUsernameDate;

    @Relationship(type = "HAS_PROFILE_PICTURE", direction = Relationship.Direction.OUTGOING)
    private FileNode profilePicture;

    @Relationship(type = "HAS_NOTIFICATION", direction = Relationship.Direction.OUTGOING)
    private List<NotificationNode> notifications = new ArrayList<>();

    @Relationship(type = "UPLOAD_FILE", direction = Relationship.Direction.OUTGOING)
    private List<FileNode> uploadedFiles = new ArrayList<>();

    @Relationship(type = "INTERACT_WITH", direction = Relationship.Direction.OUTGOING)
    private List<KeywordNode> interactedKeywords = new ArrayList<>();

    @Relationship(type = "IS_MEMBER_OF", direction = Relationship.Direction.OUTGOING)
    private List<ChatNode> chats = new ArrayList<>();

    @Relationship(type = "POSTED", direction = Relationship.Direction.OUTGOING)
    private List<PostNode> posts = new ArrayList<>();

    @Relationship(type = "LIKED", direction = Relationship.Direction.OUTGOING)
    private List<PostNode> likedPosts = new ArrayList<>();

    @Relationship(type = "COMMENTED", direction = Relationship.Direction.OUTGOING)
    private List<CommentNode> comments = new ArrayList<>();

    @Relationship(type = "LIKED", direction = Relationship.Direction.OUTGOING)
    private List<CommentNode> likedComments = new ArrayList<>();

    @Relationship(type = "FRIEND", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> friends = new ArrayList<>();

    @Relationship(type = "REQUEST", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> sentRequests = new ArrayList<>();

    @Relationship(type = "BLOCK", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> blockedUsers = new ArrayList<>();

    @Relationship(type = "SENT", direction = Relationship.Direction.OUTGOING)
    private List<MessageNode> sendUsers = new ArrayList<>();

    // ===== Constructors =====

    public UserNode() {
    }

    public UserNode(String id, String username, String familyName, String givenName,
                    LocalDate birthdate, String bio,
                    int friendCount, int requestSentCount, int requestReceivedCount,
                    int blockCount,
                    LocalDate nextChangeNameDate,
                    LocalDate nextChangeBirthdateDate,
                    LocalDate nextChangeUsernameDate) {
        this.id = id;
        this.username = username;
        this.familyName = familyName;
        this.givenName = givenName;
        this.birthdate = birthdate;
        this.bio = bio;
        this.friendCount = friendCount;
        this.requestSentCount = requestSentCount;
        this.requestReceivedCount = requestReceivedCount;
        this.blockCount = blockCount;
        this.nextChangeNameDate = nextChangeNameDate;
        this.nextChangeBirthdateDate = nextChangeBirthdateDate;
        this.nextChangeUsernameDate = nextChangeUsernameDate;
    }

    // ===== Getters / Setters =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String v) {
        this.username = v;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String v) {
        this.familyName = v;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String v) {
        this.givenName = v;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate v) {
        this.birthdate = v;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String v) {
        this.bio = v;
    }

    public int getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int v) {
        this.friendCount = v;
    }

    public int getRequestSentCount() {
        return requestSentCount;
    }

    public void setRequestSentCount(int v) {
        this.requestSentCount = v;
    }

    public int getRequestReceivedCount() {
        return requestReceivedCount;
    }

    public void setRequestReceivedCount(int v) {
        this.requestReceivedCount = v;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int v) {
        this.blockCount = v;
    }

    public LocalDate getNextChangeNameDate() {
        return nextChangeNameDate;
    }

    public void setNextChangeNameDate(LocalDate v) {
        this.nextChangeNameDate = v;
    }

    public LocalDate getNextChangeBirthdateDate() {
        return nextChangeBirthdateDate;
    }

    public void setNextChangeBirthdateDate(LocalDate v) {
        this.nextChangeBirthdateDate = v;
    }

    public LocalDate getNextChangeUsernameDate() {
        return nextChangeUsernameDate;
    }

    public void setNextChangeUsernameDate(LocalDate v) {
        this.nextChangeUsernameDate = v;
    }

    public FileNode getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(FileNode v) {
        this.profilePicture = v;
    }

    public List<NotificationNode> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationNode> v) {
        this.notifications = v;
    }

    public List<FileNode> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<FileNode> v) {
        this.uploadedFiles = v;
    }

    public List<ChatNode> getChats() {
        return chats;
    }

    public void setChats(List<ChatNode> v) {
        this.chats = v;
    }

    public List<PostNode> getPosts() {
        return posts;
    }

    public void setPosts(List<PostNode> v) {
        this.posts = v;
    }

    public List<PostNode> getLikedPosts() {
        return likedPosts;
    }

    public void setLikedPosts(List<PostNode> v) {
        this.likedPosts = v;
    }

    public List<CommentNode> getComments() {
        return comments;
    }

    public void setComments(List<CommentNode> v) {
        this.comments = v;
    }

    public List<UserNode> getFriends() {
        return friends;
    }

    public void setFriends(List<UserNode> v) {
        this.friends = v;
    }

    public List<UserNode> getSentRequests() {
        return sentRequests;
    }

    public void setSentRequests(List<UserNode> v) {
        this.sentRequests = v;
    }

    public List<UserNode> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(List<UserNode> v) {
        this.blockedUsers = v;
    }
}