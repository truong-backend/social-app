package com.socialapp.infrastructure.persistence.comment;

import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.infrastructure.persistence.comment.mapper.CommentMapper;
import com.socialapp.infrastructure.persistence.comment.neo4j.CommentNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentNeo4jRepository neo4j;
    private final CommentMapper          mapper;

    @Override public Optional<Comment> findById(String id) {
        return neo4j.findById(id).map(mapper::toDomain);
    }

    @Override public List<Comment> findByPostId(String postId, int skip, int limit) {
        return neo4j.findRootByPostId(postId, skip, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public List<Comment> findRepliesByCommentId(String commentId, int skip, int limit) {
        return neo4j.findRepliesByCommentId(commentId, skip, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public boolean isLikedByUser(String userId, String commentId) {
        return neo4j.isLikedByUser(userId, commentId);
    }

    @Override public Comment save(Comment comment) {
        return mapper.toDomain(neo4j.save(mapper.toNode(comment)));
    }

    @Override public void deleteById(String id) {
        neo4j.deleteById(id);
    }
}
