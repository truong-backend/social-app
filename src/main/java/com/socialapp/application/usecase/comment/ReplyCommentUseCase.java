package com.socialapp.application.usecase.comment;

import com.socialapp.application.dto.request.CreateCommentRequest;
import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.application.mapper.CommentMapper;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.valueobject.CommentContent;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.PostRepository;

import java.util.UUID;

public class ReplyCommentUseCase {

    private final PostRepository postRepository;
    private final CommentMapper  commentMapper;

    public ReplyCommentUseCase(PostRepository postRepository,
                               CommentMapper commentMapper) {
        this.postRepository = postRepository;
        this.commentMapper  = commentMapper;
    }

    public CommentResponse execute(String userId, String postId,
                                   String parentCommentId, CreateCommentRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Comment parent = post.getComments().stream()
                .filter(c -> c.getId().equals(parentCommentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + parentCommentId));

        Comment reply = new Comment(
                UUID.randomUUID().toString(),
                new UserId(userId),
                new CommentContent(req.content())
        );
        parent.addReply(reply);
        postRepository.save(post);

        return commentMapper.toResponse(reply);
    }
}