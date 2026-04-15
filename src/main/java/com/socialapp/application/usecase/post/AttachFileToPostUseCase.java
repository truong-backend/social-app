package com.socialapp.application.usecase.post;

import com.socialapp.application.dto.response.PostResponse;
import com.socialapp.application.mapper.PostMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.domain.model.aggregate.Post;
import com.socialapp.domain.model.valueobject.FileMeta;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.service.PostDomainService;
import org.springframework.web.multipart.MultipartFile;


public class AttachFileToPostUseCase {

    private final PostDomainService postDomainService;
    private final PostRepository    postRepository;
    private final FileStoragePort   fileStoragePort;
    private final PostMapper        postMapper;

    public AttachFileToPostUseCase(PostDomainService postDomainService,
                                   PostRepository postRepository,
                                   FileStoragePort fileStoragePort,
                                   PostMapper postMapper) {
        this.postDomainService = postDomainService;
        this.postRepository    = postRepository;
        this.fileStoragePort   = fileStoragePort;
        this.postMapper        = postMapper;
    }

    public PostResponse execute(String userId, String postId, MultipartFile file) {
        FileMeta fileMeta = fileStoragePort.store(file);
        postDomainService.attachFileToPost(postId, new UserId(userId), fileMeta);

        Post updated = postRepository.findById(postId).orElseThrow();
        return postMapper.toResponse(updated);
    }
}