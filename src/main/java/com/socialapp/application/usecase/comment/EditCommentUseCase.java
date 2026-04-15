package com.socialapp.application.usecase.comment;

import com.socialapp.application.dto.request.EditCommentRequest;
import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.application.mapper.CommentMapper;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.valueobject.CommentContent;
import com.socialapp.domain.repository.PostRepository;

public class EditCommentUseCase {

    private final PostRepository postRepository;
    private final CommentMapper  commentMapper;

    public EditCommentUseCase(PostRepository postRepository,
                              CommentMapper commentMapper) {
        this.postRepository = postRepository;
        this.commentMapper  = commentMapper;
    }

    public CommentResponse execute(String userId, String postId,
                                   String commentId, EditCommentRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        if (!comment.getAuthorId().getValue().equals(userId))
            throw new IllegalStateException("Only the author can edit this comment");

        comment.edit(new CommentContent(req.content()));
        postRepository.save(post);

        return commentMapper.toResponse(comment);
    }
}