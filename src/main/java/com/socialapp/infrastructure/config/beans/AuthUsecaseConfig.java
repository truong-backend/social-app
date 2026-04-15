package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.AccountMapper;
import com.socialapp.application.port.EmailPort;
import com.socialapp.application.port.TokenPort;
import com.socialapp.application.usecase.auth.*;
import com.socialapp.domain.repository.AccountRepository;
import com.socialapp.domain.service.AuthDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthUsecaseConfig {

    @Bean
    public RegisterUseCase registerUseCase(
            AuthDomainService authDomainService,
            AccountMapper accountMapper,
            EmailPort emailPort
    ) {
        return new RegisterUseCase(authDomainService, accountMapper, emailPort);
    }

    @Bean
    public LoginUseCase loginUseCase(
            AuthDomainService authDomainService,
            TokenPort tokenPort
    ) {
        return new LoginUseCase(authDomainService, tokenPort);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            AuthDomainService authDomainService
    ) {
        return new ChangePasswordUseCase(authDomainService);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(
            AuthDomainService authDomainService
    ) {
        return new VerifyEmailUseCase(authDomainService);
    }

    @Bean
    public ResendVerifyCodeUseCase resendVerifyCodeUseCase(
            AuthDomainService authDomainService,
            AccountRepository accountRepository,
            EmailPort emailPort
    ) {
        return new ResendVerifyCodeUseCase(
                authDomainService,
                accountRepository,
                emailPort
        );
    }
}