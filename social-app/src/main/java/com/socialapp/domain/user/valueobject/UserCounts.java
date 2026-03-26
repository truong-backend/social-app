package com.socialapp.domain.user.valueobject;
/**
 * Value Object: UserCounts
 * Gom nhóm các counter liên quan để tránh primitive obsession.
 */
public final class UserCounts {

    private final int friendCount;
    private final int blockCount;
    private final int requestSentCount;
    private final int requestReceivedCount;

    private UserCounts(int friendCount, int blockCount,
                       int requestSentCount, int requestReceivedCount) {
        this.friendCount          = friendCount;
        this.blockCount           = blockCount;
        this.requestSentCount     = requestSentCount;
        this.requestReceivedCount = requestReceivedCount;
    }

    public static UserCounts of(int friendCount, int blockCount,
                                int requestSentCount, int requestReceivedCount) {
        return new UserCounts(friendCount, blockCount, requestSentCount, requestReceivedCount);
    }

    public static UserCounts zero() {
        return new UserCounts(0, 0, 0, 0);
    }

    public UserCounts incrementFriend()            { return new UserCounts(friendCount + 1, blockCount, requestSentCount, requestReceivedCount); }
    public UserCounts decrementFriend()            { return new UserCounts(Math.max(0, friendCount - 1), blockCount, requestSentCount, requestReceivedCount); }
    public UserCounts incrementBlock()             { return new UserCounts(friendCount, blockCount + 1, requestSentCount, requestReceivedCount); }
    public UserCounts decrementBlock()             { return new UserCounts(friendCount, Math.max(0, blockCount - 1), requestSentCount, requestReceivedCount); }
    public UserCounts incrementRequestSent()       { return new UserCounts(friendCount, blockCount, requestSentCount + 1, requestReceivedCount); }
    public UserCounts decrementRequestSent()       { return new UserCounts(friendCount, blockCount, Math.max(0, requestSentCount - 1), requestReceivedCount); }
    public UserCounts incrementRequestReceived()   { return new UserCounts(friendCount, blockCount, requestSentCount, requestReceivedCount + 1); }
    public UserCounts decrementRequestReceived()   { return new UserCounts(friendCount, blockCount, requestSentCount, Math.max(0, requestReceivedCount - 1)); }

    public int getFriendCount()          { return friendCount; }
    public int getBlockCount()           { return blockCount; }
    public int getRequestSentCount()     { return requestSentCount; }
    public int getRequestReceivedCount() { return requestReceivedCount; }
}