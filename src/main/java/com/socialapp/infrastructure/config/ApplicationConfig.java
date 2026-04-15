package com.socialapp.infrastructure.config;

import com.socialapp.domain.repository.AccountRepository;
import com.socialapp.domain.repository.ChatRepository;
import com.socialapp.domain.repository.PostRepository;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring Domain Services với Repository implementations.
 * Domain Services không có @Service nên phải khai báo @Bean ở đây.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public AuthDomainService authDomainService(AccountRepository accountRepository,
                                               UserRepository userRepository,
                                               PasswordHasher passwordHasher) {
        return new AuthDomainService(accountRepository, userRepository, passwordHasher);
    }

    @Bean
    public UserProfileDomainService userProfileDomainService(UserRepository userRepository) {
        return new UserProfileDomainService(userRepository);
    }

    @Bean
    public FriendshipDomainService friendshipDomainService(UserRepository userRepository) {
        return new FriendshipDomainService(userRepository);
    }

    @Bean
    public PostDomainService postDomainService(PostRepository postRepository,
                                               UserRepository userRepository) {
        return new PostDomainService(postRepository, userRepository);
    }

    @Bean
    public ChatDomainService chatDomainService(ChatRepository chatRepository) {
        return new ChatDomainService(chatRepository);
    }
}