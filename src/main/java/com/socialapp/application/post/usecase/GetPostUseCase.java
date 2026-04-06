package com.socialapp.application.post.usecase;

import com.socialapp.application.post.dto.response.PostResponseDtos.PostResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.post.service.PostDomainService;
import com.socialapp.domain.relationship.repository.FriendRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetPostUseCase {

    private final PostRepository    postRepository;
    private final PostDomainService postDomainService;
    private final FriendRepository  friendRepository;
    private final FileStorage       fileStorage;

    public GetPostUseCase(PostRepository postRepository,
                          PostDomainService postDomainService,
                          FriendRepository friendRepository,
                          FileStorage fileStorage) {
        this.postRepository    = postRepository;
        this.postDomainService = postDomainService;
        this.friendRepository  = friendRepository;
        this.fileStorage       = fileStorage;
    }

    @Transactional(readOnly = true)
    public PostResponse execute(String requesterId, String postId) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean isFriend = friendRepository.existsFriendship(requesterId, post.getAuthorId());
        postDomainService.validateCanView(post, requesterId, isFriend);

        boolean isLiked = postRepository.isLikedByUser(requesterId, postId);

        return toResponse(post, isLiked);
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
