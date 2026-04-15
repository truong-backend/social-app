package com.socialapp.application.usecase.admin;

import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.repository.PostRepository;

public class AdminDeleteCommentUseCase {

    private final PostRepository postRepository;

    public AdminDeleteCommentUseCase(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Admin xóa bình luận — không cần kiểm tra authorId.
     */
    public void execute(String postId, String commentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        post.removeComment(commentId);
        postRepository.save(post);
    }
}