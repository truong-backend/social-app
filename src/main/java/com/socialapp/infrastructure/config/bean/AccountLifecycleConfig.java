package com.socialapp.infrastructure.config.bean;

import com.socialapp.application.account.usecase.DeleteAccountUseCase;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountLifecycleConfig {

    @Bean
    public DeleteAccountUseCase deleteAccountUseCase(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AccountDomainService accountDomainService) {
        return new DeleteAccountUseCase(accountRepository, userRepository, accountDomainService);
    }
}