package com.socialapp.application.account.usecase.login;

import com.socialapp.application.account.dto.request.AccountRequestDtos.LoginRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.AuthResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.TokenProvider;
import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class LoginUseCase {

    private final AccountRepository    accountRepository;
    private final AccountDomainService accountDomainService;
    private final TokenProvider        tokenProvider;
    private final PasswordEncoder      passwordEncoder;

    public LoginUseCase(AccountRepository accountRepository, AccountDomainService accountDomainService, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountDomainService = accountDomainService;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginRequest request) {

        // 1. Tìm account theo email
        Account account = accountRepository.findByEmail(Email.of(request.email()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        // 2. Domain validate đăng nhập (verified + password match)
        accountDomainService.validateLogin(account, request.password());

        // 3. Cấp token
        String accessToken  = tokenProvider.generateAccessToken(
                account.getId(), account.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(account.getId());

        return new AuthResponse(accessToken, refreshToken,
                account.getId(), account.getUserId(), account.getRole().name());
    }
}