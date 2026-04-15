package com.socialapp.application.usecase.comment;

import com.socialapp.application.dto.request.CreateCommentRequest;
import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.application.mapper.CommentMapper;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.valueobject.CommentContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.PostDomainService;

public class AddCommentUseCase {

    private final PostDomainService postDomainService;
    private final CommentMapper     commentMapper;

    public AddCommentUseCase(PostDomainService postDomainService,
                             CommentMapper commentMapper) {
        this.postDomainService = postDomainService;
        this.commentMapper     = commentMapper;
    }

    public CommentResponse execute(String userId, String postId, CreateCommentRequest req) {
        Comment comment = postDomainService.addComment(
                postId,
                new UserId(userId),
                new CommentContent(req.content())
        );
        return commentMapper.toResponse(comment);
    }
}