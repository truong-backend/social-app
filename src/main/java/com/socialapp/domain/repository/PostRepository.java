package com.socialapp.domain.repository;

import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository: Post
 */
public interface PostRepository {

    Optional<Post> findById(String id);

    /** Lấy bài viết của một user (cho trang cá nhân) */
    List<Post> findByAuthorId(UserId authorId, int limit, int offset);

    /** Lấy news feed: bài PUBLIC + bài bạn bè */
    List<Post> findFeedForUser(UserId userId, int limit, int offset);

    /** Tìm kiếm bài viết theo từ khóa */
    List<Post> searchByKeyword(String keyword, int limit, int offset);

    /** Kiểm tra user đã like bài viết chưa */
    boolean hasLiked(UserId userId, String postId);

    /** Tạo quan hệ LIKED trong graph */
    void addLike(UserId userId, String postId);

    /** Xóa quan hệ LIKED trong graph */
    void removeLike(UserId userId, String postId);

    void save(Post post);

    void delete(String id);

}