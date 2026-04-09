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

    void addLike(String userId, String postId);

    void removeLike(String userId, String postId);

    /** Feed ranking — trả về domain Post để UseCase xử lý filter + map */
    List<Post> findRankedFeedByUserId(String userId, int skip, int limit);
}