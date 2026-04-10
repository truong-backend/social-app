package com.socialapp.infrastructure.config.bean;

import com.socialapp.application.account.usecase.Register.RegisterUseCase;
import com.socialapp.application.shared.port.EmailSender;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthConfig {
    @Bean
    public RegisterUseCase registerUseCase(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AccountDomainService accountDomainService,
            EmailSender emailSender,
            PasswordEncoder passwordEncoder) {
        return new RegisterUseCase(accountRepository, userRepository,
                accountDomainService, emailSender, passwordEncoder);
    }
}

