package com.socialapp.application.post.usecase;

import com.socialapp.application.post.dto.response.PostResponseDtos.PostResponse;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.relationship.repository.BlockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetFeedUseCase {

    private final PostRepository  postRepository;
    private final FileStorage     fileStorage;
    private final BlockRepository blockRepository;

    public GetFeedUseCase(PostRepository postRepository,
                          FileStorage fileStorage,
                          BlockRepository blockRepository) {
        this.postRepository  = postRepository;
        this.fileStorage     = fileStorage;
        this.blockRepository = blockRepository;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> execute(String userId, int skip, int limit) {
        var blockedIds = blockRepository.findBlockedByUserId(userId)
                .stream().map(b -> b.getBlockedId()).toList();
        var blockedByIds = blockRepository.findBlockersByUserId(userId)
                .stream().map(b -> b.getBlockerId()).toList();

        return postRepository.findRankedFeedByUserId(userId, skip, limit + 20)
                .stream()
                .filter(post -> !blockedIds.contains(post.getAuthorId())
                        && !blockedByIds.contains(post.getAuthorId()))
                .limit(limit)
                .map(post -> toResponse(post,
                        postRepository.isLikedByUser(userId, post.getId())))
                .toList();
    }

    private PostResponse toResponse(Post post, boolean isLiked) {
        List<String> fileUrls = post.getAttachedFilePaths() == null
                ? List.of()
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