package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.domain.model.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return toResponse(comment, null, null);
    }

    public CommentResponse toResponse(Comment comment,
                                      String authorName,
                                      String authorAvatarUrl) {
        String attachmentUrl = (comment.getAttachedFile() != null)
                ? comment.getAttachedFile().getMeta().getPath()
                : null;

        return new CommentResponse(
                comment.getId(),
                comment.getAuthorId().getValue(),
                authorName,
                authorAvatarUrl,
                comment.getContent().getValue(),
                comment.getLikeCount(),
                comment.getReplyCount(),
                attachmentUrl,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}