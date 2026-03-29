package com.socialapp.application.account.usecase.login;

import com.socialapp.application.account.dto.request.AccountRequestDtos.ConfirmResetCodeRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.account.entity.Account;
//import com.socialapp.domain.account.entity.AccountDomainException;
import com.socialapp.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public class ConfirmResetCodeUseCase {

    private final AccountRepository accountRepository;

    public ConfirmResetCodeUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public MessageResponse execute(String accountId, ConfirmResetCodeRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Reuse confirmEmail logic — cùng flow xác nhận code
        account.confirmEmail(request.code());
        accountRepository.save(account);

        return new MessageResponse("Code confirmed. You can now reset your password.");
    }
}