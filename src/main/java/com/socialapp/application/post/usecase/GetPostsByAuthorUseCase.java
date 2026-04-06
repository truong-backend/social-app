package com.socialapp.application.post.usecase;

import com.socialapp.application.post.dto.response.PostResponseDtos.PostResponse;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetPostsByAuthorUseCase {

    private final PostRepository postRepository;
    private final FileStorage    fileStorage;

    public GetPostsByAuthorUseCase(PostRepository postRepository, FileStorage fileStorage) {
        this.postRepository = postRepository;
        this.fileStorage    = fileStorage;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> execute(String authorId, String viewerId, int skip, int limit) {
        return postRepository.findByAuthorId(authorId, viewerId, skip, limit)
                .stream()
                .map(post -> toResponse(post, postRepository.isLikedByUser(viewerId, post.getId())))
                .toList();
    }

    private PostResponse toResponse(Post post, boolean isLiked) {
        // ✅ FIX: Chuyển paths → public URLs
        List<String> fileUrls = post.getAttachedFilePaths() == null ? List.of()
                : post.getAttachedFilePaths().stream()
                        .map(fileStorage::getPublicUrl)
                        .toList();

        return new PostResponse(
                post.getId(), post.getAuthorId(), null, null,
                post.getContent(), post.getPrivacy().name(),
                post.getCounts().getLikeCount(),
                post.getCounts().getShareCount(),
                post.getCounts().getCommentCount(),
                isLiked, post.isShared(), post.getSharedFromPostId(),
                fileUrls,
                post.getCreatedAt(), post.getUpdatedAt()
        );
    }
}
