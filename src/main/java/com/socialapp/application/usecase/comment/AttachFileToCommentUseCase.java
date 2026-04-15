package com.socialapp.application.usecase.comment;

import com.socialapp.application.dto.response.CommentResponse;
import com.socialapp.application.mapper.CommentMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.entity.Comment;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.repository.PostRepository;
import org.springframework.web.multipart.MultipartFile;

public class AttachFileToCommentUseCase {

    private final PostRepository  postRepository;
    private final FileStoragePort fileStoragePort;
    private final CommentMapper   commentMapper;

    public AttachFileToCommentUseCase(PostRepository postRepository,
                                      FileStoragePort fileStoragePort,
                                      CommentMapper commentMapper) {
        this.postRepository  = postRepository;
        this.fileStoragePort = fileStoragePort;
        this.commentMapper   = commentMapper;
    }

    public CommentResponse execute(String userId, String postId,
                                   String commentId, MultipartFile file) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Comment comment = post.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        if (!comment.getAuthorId().getValue().equals(userId))
            throw new IllegalStateException("Only the author can attach a file to this comment");

        FileMeta fileMeta = fileStoragePort.store(file);
        // Rule: chỉ 1 file — attachFile() overwrite file cũ trong entity
        comment.attachFile(new FileEntity(fileMeta));
        postRepository.save(post);

        return commentMapper.toResponse(comment);
    }
}