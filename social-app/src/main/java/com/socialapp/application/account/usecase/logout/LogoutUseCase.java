package com.socialapp.application.account.usecase.logout;

import com.socialapp.application.account.dto.response.AccountResponseDtos.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * LogoutUseCase
 * JWT là stateless — logout chỉ cần blacklist token ở infrastructure.
 * Domain không cần làm gì thêm.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final TokenBlacklist tokenBlacklist;

    public MessageResponse execute(String accessToken) {
        tokenBlacklist.revoke(accessToken);
        return new MessageResponse("Logged out successfully");
    }

    /** Outbound port — implement ở infrastructure (Redis / in-memory) */
    public interface TokenBlacklist {
        void revoke(String token);
        boolean isRevoked(String token);
    }
}