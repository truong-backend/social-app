package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.PostMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.application.usecase.post.*;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.service.PostDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostUsecaseConfig {

    @Bean
    public CreatePostUseCase createPostUseCase(
            PostDomainService postDomainService,
            PostMapper postMapper
    ) {
        return new CreatePostUseCase(postDomainService, postMapper);
    }

    @Bean
    public EditPostUseCase editPostUseCase(
            PostDomainService postDomainService,
            PostRepository postRepository,
            PostMapper postMapper
    ) {
        return new EditPostUseCase(postDomainService, postRepository, postMapper);
    }

    @Bean
    public DeletePostUseCase deletePostUseCase(
            PostDomainService postDomainService
    ) {
        return new DeletePostUseCase(postDomainService);
    }

    @Bean
    public GetFeedUseCase getFeedUseCase(
            PostRepository postRepository,
            PostMapper postMapper
    ) {
        return new GetFeedUseCase(postRepository, postMapper);
    }

    @Bean
    public GetUserPostsUseCase getUserPostsUseCase(
            PostRepository postRepository,
            PostMapper postMapper
    ) {
        return new GetUserPostsUseCase(postRepository, postMapper);
    }

    @Bean
    public LikePostUseCase likePostUseCase(
            PostDomainService postDomainService
    ) {
        return new LikePostUseCase(postDomainService);
    }

    @Bean
    public SearchPostUseCase searchPostUseCase(
            PostRepository postRepository,
            PostMapper postMapper
    ) {
        return new SearchPostUseCase(postRepository, postMapper);
    }

    @Bean
    public SharePostUseCase sharePostUseCase(
            PostDomainService postDomainService,
            PostMapper postMapper
    ) {
        return new SharePostUseCase(postDomainService, postMapper);
    }

    @Bean
    public AttachFileToPostUseCase attachFileToPostUseCase(
            PostDomainService postDomainService,
            PostRepository postRepository,
            FileStoragePort fileStoragePort,
            PostMapper postMapper
    ) {
        return new AttachFileToPostUseCase(
                postDomainService,
                postRepository,
                fileStoragePort,
                postMapper
        );
    }
}