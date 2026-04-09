package com.socialapp.application.report.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.account.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

public class BanUserUseCase {

    private final AccountRepository accountRepository;

    public BanUserUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void execute(String targetUserId, String reason) {
        var account = accountRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.ban(reason);
        accountRepository.save(account);
    }
}