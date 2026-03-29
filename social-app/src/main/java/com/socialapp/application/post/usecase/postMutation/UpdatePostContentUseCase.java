package com.socialapp.application.post.usecase.postMutation;

import com.socialapp.application.post.dto.request.PostRequestDtos;
import com.socialapp.application.post.dto.response.PostResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


public class UpdatePostContentUseCase {

    private final PostRepository postRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    public UpdatePostContentUseCase(PostRepository postRepository, FileStorage fileStorage, FileRepository fileRepository) {
        this.postRepository = postRepository;
        this.fileStorage = fileStorage;
        this.fileRepository = fileRepository;
    }

    @Transactional
    public PostResponseDtos.PostResponse execute(String requesterId, String postId,
                                                 PostRequestDtos.UpdatePostContentRequest request,
                                                 List<MultipartFile> newFiles) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Xóa files cũ
        fileStorage.deleteAll(post.getAttachedFilePaths());
        fileRepository.deleteByPaths(post.getAttachedFilePaths());

        // Upload files mới
        List<String> newPaths = new ArrayList<>();
        if (newFiles != null) {
            for (MultipartFile f : newFiles) {
                String path = fileStorage.upload(f);
                fileRepository.save(FileNode.create(path, f.getOriginalFilename(), f.getContentType()));
                newPaths.add(path);
            }
        }

        // Domain behavior
        post.updateContent(requesterId, request.content(), newPaths);
        postRepository.save(post);

        return toResponse(post);
    }

    private PostResponseDtos.PostResponse toResponse(Post post) {
        return new PostResponseDtos.PostResponse(post.getId(), post.getAuthorId(), null, null,
                post.getContent(), post.getPrivacy().name(),
                post.getCounts().getLikeCount(), post.getCounts().getShareCount(),
                post.getCounts().getCommentCount(), false, post.isShared(),
                post.getSharedFromPostId(), post.getAttachedFilePaths(),
                post.getCreatedAt(), post.getUpdatedAt());
    }
}
