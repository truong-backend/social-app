package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.PostMapper;
import com.socialapp.application.mapper.UserMapper;
import com.socialapp.application.usecase.admin.*;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.application.port.AdminStatsPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminUsecaseConfig {

    @Bean
    public AdminListUsersUseCase adminListUsersUseCase(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        return new AdminListUsersUseCase(userRepository, userMapper);
    }

    @Bean
    public AdminListPostsUseCase adminListPostsUseCase(
            PostRepository postRepository,
            PostMapper postMapper
    ) {
        return new AdminListPostsUseCase(postRepository, postMapper);
    }

    @Bean
    public AdminGetStatsUseCase adminGetStatsUseCase(
            AdminStatsPort adminStatsPort
    ) {
        return new AdminGetStatsUseCase(adminStatsPort);
    }

    @Bean
    public AdminDeletePostUseCase adminDeletePostUseCase(
            PostRepository postRepository
    ) {
        return new AdminDeletePostUseCase(postRepository);
    }

    @Bean
    public AdminDeleteCommentUseCase adminDeleteCommentUseCase(
            PostRepository postRepository
    ) {
        return new AdminDeleteCommentUseCase(postRepository);
    }
}