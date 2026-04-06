package com.socialapp.infrastructure.persistence.post.neo4j;

import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.infrastructure.persistence.post.mapper.PostMapper;
import com.socialapp.infrastructure.persistence.post.neo4j.node.PostNode;
import com.socialapp.infrastructure.persistence.post.neo4j.repository.PostNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostNeo4jRepository neo4jRepository;
    private final PostMapper          mapper;

    @Override
    public Optional<Post> findById(String id) {
        return neo4jRepository.findById(id).map(node -> resolveFullPost(node));
    }

    @Override
    public Optional<Post> findByIdNotDeleted(String id) {
        return neo4jRepository.findByIdNotDeleted(id).map(node -> resolveFullPost(node));
    }

    @Override
    public List<Post> findFeedByUserId(String userId, int skip, int limit) {
        return neo4jRepository.findFeedByUserId(userId, skip, limit)
                .stream().map(node -> resolveFullPost(node)).toList();
    }

    @Override
    public List<Post> findByAuthorId(String authorId, String viewerId, int skip, int limit) {
        return neo4jRepository.findByAuthorId(authorId, viewerId, skip, limit)
                .stream().map(node -> resolveFullPost(node)).toList();
    }

    @Override
    public List<Post> searchByKeyword(String keyword, String requesterId) {
        return neo4jRepository.searchByKeyword(keyword, requesterId)
                .stream().map(node -> resolveFullPost(node)).toList();
    }

    @Override
    public boolean isLikedByUser(String userId, String postId) {
        return neo4jRepository.isLikedByUser(userId, postId);
    }

    @Override
    public Post save(Post post) {
        PostNode saved = neo4jRepository.save(mapper.toNode(post));
        String postId = saved.getId();

        // (User)-[:POSTED]→(Post)
        neo4jRepository.linkAuthorToPost(post.getAuthorId(), postId);

        // (Post)-[:SHARE]→(Post)
        if (post.isShared() && post.getSharedFromPostId() != null) {
            neo4jRepository.linkSharePost(postId, post.getSharedFromPostId());
        }

        // (Post)-[:ATTACH_FILE]→(File)
        if (post.getAttachedFilePaths() != null) {
            post.getAttachedFilePaths()
                    .forEach(path -> neo4jRepository.linkPostAttachFile(postId, path));
        }

        // (Post)-[:HAS_KEYWORD]→(Keyword)
        if (post.getKeywords() != null) {
            post.getKeywords()
                    .forEach(kw -> neo4jRepository.linkPostKeyword(postId, kw));
        }

        return mapper.toDomain(saved,
                post.getSharedFromPostId(),
                post.getAttachedFilePaths(),
                post.getKeywords());
    }

    @Override
    public void deleteById(String id) {
        neo4jRepository.deleteById(id);
    }

    // ✅ FIX: Tạo relationship LIKED — đây là điều kiện để isLikedByUser trả về đúng
    @Override
    public void addLike(String userId, String postId) {
        neo4jRepository.linkUserLikedPost(userId, postId);
    }

    // ✅ FIX: Xóa relationship LIKED khi unlike
    @Override
    public void removeLike(String userId, String postId) {
        neo4jRepository.unlinkUserLikedPost(userId, postId);
    }

    // ── Helper: resolve đầy đủ context từ graph ──────────────

    private Post resolveFullPost(PostNode node) {
        String postId             = node.getId();
        String sharedFromPostId   = neo4jRepository.findSharedFromPostId(postId).orElse(null);
        List<String> filePaths    = neo4jRepository.findAttachedFilePathsByPostId(postId);
        List<String> keywords     = neo4jRepository.findKeywordsByPostId(postId);
        return mapper.toDomain(node, sharedFromPostId, filePaths, keywords);
    }
}