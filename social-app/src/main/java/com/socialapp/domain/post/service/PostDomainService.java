package com.socialapp.domain.post.service;

import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.exception.PostDomainException;
import com.socialapp.domain.post.valueobject.Privacy;

/**
 * Domain Service: PostDomainService
 *
 * Logic cần context ngoài entity:
 *  - Validate xem bài viết (check friend + privacy)
 *  - Validate like / unlike
 */
public class PostDomainService {

    /**
     * Validate người dùng có thể xem post không
     */
    public void validateCanView(Post post, String viewerId, boolean isFriend) {
        if (post.isDeleted())
            throw new PostDomainException("Post not found");
        if (!post.isVisibleTo(viewerId, isFriend))
            throw new PostDomainException("You do not have permission to view this post");
    }

    /**
     * Validate like: chưa like trước đó
     */
    public void validateLike(boolean alreadyLiked) {
        if (alreadyLiked)
            throw new PostDomainException("You have already liked this post");
    }

    /**
     * Validate unlike: đã like trước đó
     */
    public void validateUnlike(boolean alreadyLiked) {
        if (!alreadyLiked)
            throw new PostDomainException("You have not liked this post");
    }

    /**
     * Validate share: bài gốc tồn tại và là PUBLIC
     */
    public void validateShare(Post originalPost) {
        if (originalPost.isDeleted())
            throw new PostDomainException("Cannot share a deleted post");
        if (originalPost.getPrivacy() != Privacy.PUBLIC)
            throw new PostDomainException("Can only share public posts");
    }
}
