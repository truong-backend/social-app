package com.socialapp.application.post.usecase.postMutation;

import com.socialapp.application.post.dto.response.PostResponseDtos;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.post.entity.Post;
import com.socialapp.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostUseCase {

    private final PostRepository postRepository;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    @Transactional
    public PostResponseDtos.MessageResponse execute(String requesterId, String postId, boolean isAdmin) {

        Post post = postRepository.findByIdNotDeleted(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Domain enforce author / admin check
        post.delete(requesterId, isAdmin);

        // Xóa files đính kèm
        fileStorage.deleteAll(post.getAttachedFilePaths());
        fileRepository.deleteByPaths(post.getAttachedFilePaths());

        postRepository.save(post);

        return new PostResponseDtos.MessageResponse("Post deleted successfully");
    }
}
