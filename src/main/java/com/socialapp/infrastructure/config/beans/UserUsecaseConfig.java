package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.UserMapper;
import com.socialapp.application.port.FileStoragePort;
import com.socialapp.application.usecase.user.*;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.domain.service.UserProfileDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserUsecaseConfig {

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        return new GetUserProfileUseCase(userRepository, userMapper);
    }

    @Bean
    public SearchUserUseCase searchUserUseCase(
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        return new SearchUserUseCase(userRepository, userMapper);
    }

    @Bean
    public UpdateProfileUseCase updateProfileUseCase(
            UserProfileDomainService userProfileDomainService,
            UserRepository userRepository,
            UserMapper userMapper
    ) {
        return new UpdateProfileUseCase(
                userProfileDomainService,
                userRepository,
                userMapper
        );
    }

    @Bean
    public UpdateProfilePictureUseCase updateProfilePictureUseCase(
            UserProfileDomainService userProfileDomainService,
            UserRepository userRepository,
            FileStoragePort fileStoragePort,
            UserMapper userMapper
    ) {
        return new UpdateProfilePictureUseCase(
                userProfileDomainService,
                userRepository,
                fileStoragePort,
                userMapper
        );
    }
}