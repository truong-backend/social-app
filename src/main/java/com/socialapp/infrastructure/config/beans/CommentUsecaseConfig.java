package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.CommentMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.application.usecase.comment.*;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.service.PostDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommentUsecaseConfig {

    @Bean
    public AddCommentUseCase addCommentUseCase(
            PostDomainService postDomainService,
            CommentMapper commentMapper
    ) {
        return new AddCommentUseCase(postDomainService, commentMapper);
    }

    @Bean
    public ReplyCommentUseCase replyCommentUseCase(
            PostRepository postRepository,
            CommentMapper commentMapper
    ) {
        return new ReplyCommentUseCase(postRepository, commentMapper);
    }

    @Bean
    public EditCommentUseCase editCommentUseCase(
            PostRepository postRepository,
            CommentMapper commentMapper
    ) {
        return new EditCommentUseCase(postRepository, commentMapper);
    }

    @Bean
    public DeleteCommentUseCase deleteCommentUseCase(
            PostDomainService postDomainService
    ) {
        return new DeleteCommentUseCase(postDomainService);
    }

    @Bean
    public LikeCommentUseCase likeCommentUseCase(
            PostDomainService postDomainService
    ) {
        return new LikeCommentUseCase(postDomainService);
    }

    @Bean
    public AttachFileToCommentUseCase attachFileToCommentUseCase(
            PostRepository postRepository,
            FileStoragePort fileStoragePort,
            CommentMapper commentMapper
    ) {
        return new AttachFileToCommentUseCase(
                postRepository,
                fileStoragePort,
                commentMapper
        );
    }
}