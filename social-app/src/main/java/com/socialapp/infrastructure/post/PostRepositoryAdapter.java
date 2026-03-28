package com.socialapp.infrastructure.post;

import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.infrastructure.persistence.post.mapper.PostMapper;
import com.socialapp.infrastructure.persistence.post.neo4j.PostNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostNeo4jRepository neo4jRepository;
    private final PostMapper          mapper;

    @Override public Optional<Post> findById(String id) {
        return neo4jRepository.findById(id).map(mapper::toDomain);
    }

    @Override public Optional<Post> findByIdNotDeleted(String id) {
        return neo4jRepository.findByIdNotDeleted(id).map(mapper::toDomain);
    }

    @Override public List<Post> findFeedByUserId(String userId, int skip, int limit) {
        return neo4jRepository.findFeedByUserId(userId, skip, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public List<Post> findByAuthorId(String authorId, String viewerId, int skip, int limit) {
        return neo4jRepository.findByAuthorId(authorId, viewerId, skip, limit)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public List<Post> searchByKeyword(String keyword, String requesterId) {
        return neo4jRepository.searchByKeyword(keyword, requesterId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override public boolean isLikedByUser(String userId, String postId) {
        return neo4jRepository.isLikedByUser(userId, postId);
    }

    @Override public Post save(Post post) {
        return mapper.toDomain(neo4jRepository.save(mapper.toNode(post)));
    }

    @Override public void deleteById(String id) {
        neo4jRepository.deleteById(id);
    }
}