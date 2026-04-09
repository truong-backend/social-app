package com.socialapp.infrastructure.config.jwt;

import com.socialapp.application.account.usecase.login.RefreshTokenUseCase;
import com.socialapp.application.shared.port.TokenProvider;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.repository.RefreshTokenRepository;
import com.socialapp.infrastructure.persistence.account.neo4j.RefreshTokenRepositoryAdapter;
import com.socialapp.infrastructure.persistence.account.neo4j.repository.RefreshTokenNeo4jRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefreshTokenConfig {

    @Bean
    public RefreshTokenRepository refreshTokenRepository(
            RefreshTokenNeo4jRepository neo4jRepository) {
        return new RefreshTokenRepositoryAdapter(neo4jRepository);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            RefreshTokenRepository refreshTokenRepository,
            AccountRepository accountRepository,
            TokenProvider tokenProvider) {
        return new RefreshTokenUseCase(refreshTokenRepository, accountRepository, tokenProvider);
    }
}