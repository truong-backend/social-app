package com.socialapp.application.usecase.admin;

import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.repository.PostRepository;

public class AdminDeletePostUseCase {

    private final PostRepository postRepository;

    public AdminDeletePostUseCase(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Admin xóa bài viết — không cần kiểm tra authorId.
     */
    public void execute(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        post.softDelete();
        postRepository.save(post);
    }
}