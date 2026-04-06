package com.socialapp.infrastructure.persistence.comment.neo4j;

import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.infrastructure.persistence.comment.mapper.CommentMapper;
import com.socialapp.infrastructure.persistence.comment.neo4j.node.CommentNode;
import com.socialapp.infrastructure.persistence.comment.neo4j.repository.CommentNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentNeo4jRepository neo4j;
    private final CommentMapper          mapper;

    @Override
    public Optional<Comment> findById(String id) {
        return neo4j.findById(id).map(node -> {
            String authorId           = neo4j.findAuthorIdByCommentId(node.getId());
            String postId             = neo4j.findPostIdByCommentId(node.getId());
            String repliedToCommentId = neo4j.findRepliedToCommentId(node.getId());
            List<String> filePaths    = neo4j.findAttachedFilePathsByCommentId(node.getId());
            return mapper.toDomain(node, authorId, postId, repliedToCommentId, filePaths);
        });
    }

    @Override
    public List<Comment> findByPostId(String postId, int skip, int limit) {
        return neo4j.findRootByPostId(postId, skip, limit)
                .stream()
                .map(node -> {
                    String authorId           = neo4j.findAuthorIdByCommentId(node.getId());
                    String repliedToCommentId = neo4j.findRepliedToCommentId(node.getId());
                    List<String> filePaths    = neo4j.findAttachedFilePathsByCommentId(node.getId());
                    return mapper.toDomain(node, authorId, postId, repliedToCommentId, filePaths);
                })
                .toList();
    }

    @Override
    public List<Comment> findRepliesByCommentId(String commentId, int skip, int limit) {
        return neo4j.findRepliesByCommentId(commentId, skip, limit)
                .stream()
                .map(node -> {
                    String authorId        = neo4j.findAuthorIdByCommentId(node.getId());
                    String postId          = neo4j.findPostIdByCommentId(node.getId());
                    List<String> filePaths = neo4j.findAttachedFilePathsByCommentId(node.getId());
                    return mapper.toDomain(node, authorId, postId, commentId, filePaths);
                })
                .toList();
    }

    @Override
    public boolean isLikedByUser(String userId, String commentId) {
        return neo4j.isLikedByUser(userId, commentId);
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        CommentNode saved = neo4j.save(mapper.toNode(comment));
        String commentId = saved.getId();

        // (Post)-[:HAS_COMMENT]→(Comment)
        neo4j.linkPostToComment(comment.getPostId(), commentId);

        // (User)-[:COMMENTED]→(Comment)
        neo4j.linkUserCommented(comment.getAuthorId(), commentId);

        // (Comment)-[:REPLIED]→(Comment) — chỉ khi là reply
        if (comment.getRepliedToCommentId() != null) {
            neo4j.linkCommentReplied(commentId, comment.getRepliedToCommentId());
        }

        // (Comment)-[:ATTACH_FILE]→(File)
        if (comment.getAttachedFilePaths() != null) {
            comment.getAttachedFilePaths()
                    .forEach(path -> neo4j.linkCommentAttachFile(commentId, path));
        }

        return mapper.toDomain(saved,
                comment.getAuthorId(),
                comment.getPostId(),
                comment.getRepliedToCommentId(),
                comment.getAttachedFilePaths());
    }

    @Override
    public void deleteById(String id) {
        neo4j.deleteById(id);
    }

    // ✅ FIX: Tạo LIKED relationship cho comment
    @Override
    public void addLike(String userId, String commentId) {
        neo4j.linkUserLikedComment(userId, commentId);
    }

    // ✅ FIX: Xóa LIKED relationship khi unlike comment
    @Override
    public void removeLike(String userId, String commentId) {
        neo4j.unlinkUserLikedComment(userId, commentId);
    }
}
