package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.response.CommentResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.post.repository.PostRepository;


public class DeleteCommentUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    public DeleteCommentUseCase(CommentRepository commentRepository, PostRepository postRepository, FileStorage fileStorage, FileRepository fileRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.fileStorage = fileStorage;
        this.fileRepository = fileRepository;
    }

    public CommentResponseDtos.MessageResponse execute(String requesterId, String commentId, boolean isAdmin) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.delete(requesterId, isAdmin);

        fileStorage.deleteAll(comment.getAttachedFilePaths());
        fileRepository.deleteByPaths(comment.getAttachedFilePaths());
        commentRepository.deleteById(commentId);

        // Giảm commentCount trên post
        postRepository.findByIdNotDeleted(comment.getPostId()).ifPresent(post -> {
            post.onCommentRemoved();
            postRepository.save(post);
        });

        return new CommentResponseDtos.MessageResponse("Comment deleted successfully");
    }
}
