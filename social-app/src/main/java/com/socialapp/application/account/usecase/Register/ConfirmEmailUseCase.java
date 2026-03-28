package com.socialapp.application.account.usecase.Register;

import com.socialapp.application.account.dto.request.AccountRequestDtos.ConfirmEmailRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.AuthResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.TokenProvider;
import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmEmailUseCase {

    private final AccountRepository accountRepository;
    private final TokenProvider     tokenProvider;

    @Transactional
    public AuthResponse execute(String accountId, ConfirmEmailRequest request) {

        // 1. Load account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // 2. Domain thực hiện xác thực (throws nếu sai / hết hạn)
        account.confirmEmail(request.code());
        accountRepository.save(account);

        // 3. Cấp token
        String accessToken  = tokenProvider.generateAccessToken(
                account.getId(), account.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(account.getId());

        return new AuthResponse(accessToken, refreshToken,
                account.getId(), account.getUserId(), account.getRole().name());
    }
}
