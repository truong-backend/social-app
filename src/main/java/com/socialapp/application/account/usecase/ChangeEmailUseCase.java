package com.socialapp.application.account.usecase;

import com.socialapp.application.shared.exception.ConflictException;
import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.shared.valueobject.Email;
import org.springframework.transaction.annotation.Transactional;

public class ChangeEmailUseCase {

    private final AccountRepository    accountRepository;
    private final AccountDomainService domainService;

    public ChangeEmailUseCase(AccountRepository accountRepository,
                              AccountDomainService domainService) {
        this.accountRepository = accountRepository;
        this.domainService     = domainService;
    }

    @Transactional
    public void execute(String accountId, String newEmail, String password) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!domainService.verifyPassword(account, password)) {
            throw new ForbiddenException("Incorrect password");
        }

        Email email = new Email(newEmail);
        boolean taken = accountRepository.existsByEmail(email.getValue());
        if (taken) {
            throw new ConflictException("Email already in use");
        }

        account.changeEmail(email.getValue());
        accountRepository.save(account);
    }
}