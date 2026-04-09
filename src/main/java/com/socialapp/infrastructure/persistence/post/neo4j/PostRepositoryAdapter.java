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
        return neo4jRepository.findById(id).map(this::resolveFullPost);
    }

    @Override
    public Optional<Post> findByIdNotDeleted(String id) {
        return neo4jRepository.findByIdNotDeleted(id).map(this::resolveFullPost);
    }

    @Override
    public List<Post> findFeedByUserId(String userId, int skip, int limit) {
        return neo4jRepository.findFeedByUserId(userId, skip, limit)
                .stream().map(this::resolveFullPost).toList();
    }

    @Override
    public List<Post> findByAuthorId(String authorId, String viewerId, int skip, int limit) {
        return neo4jRepository.findByAuthorId(authorId, viewerId, skip, limit)
                .stream().map(this::resolveFullPost).toList();
    }

    @Override
    public List<Post> searchByKeyword(String keyword, String requesterId) {
        return neo4jRepository.searchByKeyword(keyword, requesterId)
                .stream().map(this::resolveFullPost).toList();
    }

    @Override
    public boolean isLikedByUser(String userId, String postId) {
        return neo4jRepository.isLikedByUser(userId, postId);
    }

    @Override
    public Post save(Post post) {
        PostNode saved = neo4jRepository.save(mapper.toNode(post));
        String   postId = saved.getId();

        neo4jRepository.linkAuthorToPost(post.getAuthorId(), postId);

        if (post.isShared() && post.getSharedFromPostId() != null) {
            neo4jRepository.linkSharePost(postId, post.getSharedFromPostId());
        }
        if (post.getAttachedFilePaths() != null) {
            post.getAttachedFilePaths()
                    .forEach(path -> neo4jRepository.linkPostAttachFile(postId, path));
        }
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

    @Override
    public void addLike(String userId, String postId) {
        neo4jRepository.linkUserLikedPost(userId, postId);
    }

    @Override
    public void removeLike(String userId, String postId) {
        neo4jRepository.unlinkUserLikedPost(userId, postId);
    }

    /**
     * Ranked feed — trả về List<Post> (domain), UseCase tự filter + convert sang PostResponse.
     */
    @Override
    public List<Post> findRankedFeedByUserId(String userId, int skip, int limit) {
        return neo4jRepository.findRankedFeedByUserId(userId, skip, limit)
                .stream()
                .map(this::resolveFullPost)
                .toList();
    }

    // ── Helper ────────────────────────────────────────────────

    private Post resolveFullPost(PostNode node) {
        String       postId           = node.getId();
        String       sharedFromPostId = neo4jRepository.findSharedFromPostId(postId).orElse(null);
        List<String> filePaths        = neo4jRepository.findAttachedFilePathsByPostId(postId);
        List<String> keywords         = neo4jRepository.findKeywordsByPostId(postId);
        return mapper.toDomain(node, sharedFromPostId, filePaths, keywords);
    }
}