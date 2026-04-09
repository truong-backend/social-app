package com.socialapp.application.account.usecase.login;

import com.socialapp.application.account.dto.response.AccountResponseDtos.AuthResponse;
import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.port.TokenProvider;
import com.socialapp.domain.account.entity.RefreshToken;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;

public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountRepository      accountRepository;
    private final TokenProvider          tokenProvider;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               AccountRepository accountRepository,
                               TokenProvider tokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountRepository      = accountRepository;
        this.tokenProvider          = tokenProvider;
    }

    @Transactional
    public AuthResponse execute(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ForbiddenException("Invalid refresh token"));

        if (!token.isValid()) {
            throw new ForbiddenException("Refresh token expired or revoked");
        }

        var account = accountRepository.findById(token.getAccountId())
                .orElseThrow(() -> new ForbiddenException("Account not found"));

        // Revoke old token, issue new access token
        token.revoke();
        refreshTokenRepository.save(token);

        // Issue new refresh token (rotation)
        RefreshToken newRefresh = RefreshToken.create(account.getId(), 30);
        refreshTokenRepository.save(newRefresh);

        String accessToken = tokenProvider.generateAccessToken(
                account.getId(), account.getRole().name());

        return new AuthResponse(
                accessToken,
                newRefresh.getToken(),
                account.getId(),
                account.getUserId(),
                account.getRole().name()
        );
    }
}