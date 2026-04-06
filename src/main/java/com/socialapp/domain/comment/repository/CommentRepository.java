package com.socialapp.domain.comment.repository;

import com.socialapp.domain.comment.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(String id);

    List<Comment> findByPostId(String postId, int skip, int limit);

    List<Comment> findRepliesByCommentId(String commentId, int skip, int limit);

    boolean isLikedByUser(String userId, String commentId);

    Comment save(Comment comment);

    void deleteById(String id);

    /**
     * Tạo relationship (User)-[:LIKED]->(Comment) trong graph.
     * Gọi sau khi save comment (likeCount đã tăng).
     */
    void addLike(String userId, String commentId);

    /**
     * Xóa relationship (User)-[:LIKED]->(Comment) khỏi graph.
     * Gọi sau khi save comment (likeCount đã giảm).
     */
    void removeLike(String userId, String commentId);
}
