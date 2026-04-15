package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.domain.model.aggregate.Post;
import org.springframework.stereotype.Component;
import com.socialapp.application.dto.response.CommentResponse;

import java.util.List;

@Component
public class PostMapper {

    private final CommentMapper commentMapper;

    public PostMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public PostResponse toResponse(Post post) {
        return toResponse(post, null, null, null);
    }

    public PostResponse toResponse(Post post, String authorName,
                                   String authorAvatarUrl, List<String> attachmentUrls) {
        List<String> urls = (attachmentUrls != null) ? attachmentUrls
                : post.getAttachments().stream().map(f -> f.getMeta().getPath()).toList();

        List<CommentResponse> comments = post.getComments().stream()
                .map(commentMapper::toResponse)
                .toList();  // ← THÊM

        return new PostResponse(
                post.getId(), post.getAuthorId().getValue(),
                authorName, authorAvatarUrl,
                post.getContent().getValue(), post.getPrivacy(),
                post.getLikeCount(), post.getShareCount(), post.getCommentCount(),
                urls, post.getSharedFromPostId(),
                post.getCreatedAt(), post.getUpdatedAt(),
                comments   // ← THÊM
        );
    }
}