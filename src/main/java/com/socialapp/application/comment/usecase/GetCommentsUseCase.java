package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos.CommentResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.post.repository.PostRepository;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GetCommentsUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository    postRepository;
    private final FileStorage       fileStorage;
    // FIX: cần UserRepository để lấy username + avatar cho từng comment
    private final UserRepository    userRepository;

    public GetCommentsUseCase(CommentRepository commentRepository,
                              PostRepository postRepository,
                              FileStorage fileStorage,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository    = postRepository;
        this.fileStorage       = fileStorage;
        this.userRepository    = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> execute(String requesterId, String postId, int skip, int limit) {

        postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        return commentRepository.findByPostId(postId, skip, limit)
                .stream()
                .map(c -> toResponse(c, commentRepository.isLikedByUser(requesterId, c.getId())))
                .toList();
    }

    private CommentResponse toResponse(Comment c, boolean isLiked) {
        // FIX: convert file paths → public URLs để ảnh bình luận hiển thị được
        List<String> fileUrls = c.getAttachedFilePaths() == null ? List.of()
                : c.getAttachedFilePaths().stream()
                .map(fileStorage::getPublicUrl)
                .toList();

        // FIX: lấy username + profile picture của tác giả comment
        User author = userRepository.findById(c.getAuthorId()).orElse(null);
        String authorUsername   = author != null ? author.getUsername().getValue() : null;
        String rawPicPath       = author != null ? author.getProfilePicturePath() : null;
        String authorProfilePic = (rawPicPath != null && !rawPicPath.isBlank())
                ? fileStorage.getPublicUrl(rawPicPath)
                : null;

        return new CommentResponse(
                c.getId(), c.getAuthorId(), authorUsername, authorProfilePic,
                c.getPostId(), c.getRepliedToCommentId(),
                c.getContent(), fileUrls,
                c.getLikeCount(), c.getReplyCount(),
                isLiked, c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}