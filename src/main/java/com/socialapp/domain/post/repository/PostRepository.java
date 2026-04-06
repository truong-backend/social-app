package com.socialapp.domain.post.repository;

import com.socialapp.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findById(String id);

    Optional<Post> findByIdNotDeleted(String id);

    List<Post> findFeedByUserId(String userId, int skip, int limit);

    List<Post> findByAuthorId(String authorId, String viewerId, int skip, int limit);

    List<Post> searchByKeyword(String keyword, String requesterId);

    boolean isLikedByUser(String userId, String postId);

    Post save(Post post);

    void deleteById(String id);

    /**
     * Tạo relationship (User)-[:LIKED]->(Post) trong graph.
     * Gọi sau khi save post (likeCount đã tăng).
     */
    void addLike(String userId, String postId);

    /**
     * Xóa relationship (User)-[:LIKED]->(Post) khỏi graph.
     * Gọi sau khi save post (likeCount đã giảm).
     */
    void removeLike(String userId, String postId);
}
