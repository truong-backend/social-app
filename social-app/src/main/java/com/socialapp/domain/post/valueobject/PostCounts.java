package com.socialapp.domain.post.valueobject;

/**
 * Value Object: PostCounts
 */
public final class PostCounts {

    private final int likeCount;
    private final int shareCount;
    private final int commentCount;

    private PostCounts(int likeCount, int shareCount, int commentCount) {
        this.likeCount    = Math.max(0, likeCount);
        this.shareCount   = Math.max(0, shareCount);
        this.commentCount = Math.max(0, commentCount);
    }

    public static PostCounts of(int likeCount, int shareCount, int commentCount) {
        return new PostCounts(likeCount, shareCount, commentCount);
    }

    public static PostCounts zero() {
        return new PostCounts(0, 0, 0);
    }

    public PostCounts incrementLike()     { return new PostCounts(likeCount + 1, shareCount, commentCount); }
    public PostCounts decrementLike()     { return new PostCounts(likeCount - 1, shareCount, commentCount); }
    public PostCounts incrementShare()    { return new PostCounts(likeCount, shareCount + 1, commentCount); }
    public PostCounts incrementComment()  { return new PostCounts(likeCount, shareCount, commentCount + 1); }
    public PostCounts decrementComment()  { return new PostCounts(likeCount, shareCount, commentCount - 1); }

    public int getLikeCount()    { return likeCount; }
    public int getShareCount()   { return shareCount; }
    public int getCommentCount() { return commentCount; }
}