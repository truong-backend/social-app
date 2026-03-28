package com.socialapp.application.comment.usecase;

import com.socialapp.application.comment.dto.request.CommentRequestDtos;
import com.socialapp.application.comment.dto.response.CommentResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.comment.entity.Comment;
import com.socialapp.domain.comment.repository.CommentRepository;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
class UpdateCommentUseCase {

    private final CommentRepository commentRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    @Transactional
    public CommentResponseDtos.CommentResponse execute(String requesterId, String commentId,
                                                       CommentRequestDtos.UpdateCommentRequest request,
                                                       List<MultipartFile> newFiles) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        fileStorage.deleteAll(comment.getAttachedFilePaths());
        fileRepository.deleteByPaths(comment.getAttachedFilePaths());

        List<String> paths = new ArrayList<>();
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                String path = fileStorage.upload(f);
                fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
                paths.add(path);
            }
        }

        comment.updateContent(requesterId, request.content(), paths);
        commentRepository.save(comment);

        return new CommentResponseDtos.CommentResponse(comment.getId(), comment.getAuthorId(), null, null,
                comment.getPostId(), comment.getRepliedToCommentId(), comment.getContent(),
                comment.getAttachedFilePaths(), comment.getLikeCount(), comment.getReplyCount(),
                false, comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
