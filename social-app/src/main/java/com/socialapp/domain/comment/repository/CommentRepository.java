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
}