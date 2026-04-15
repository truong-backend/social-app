package com.socialapp.application.account.usecase;

import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

public class DeleteAccountUseCase {

    private final AccountRepository     accountRepository;
    private final UserRepository        userRepository;
    private final AccountDomainService  domainService;

    public DeleteAccountUseCase(AccountRepository accountRepository,
                                UserRepository userRepository,
                                AccountDomainService domainService) {
        this.accountRepository = accountRepository;
        this.userRepository    = userRepository;
        this.domainService     = domainService;
    }

    @Transactional
    public void execute(String accountId, String password) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Xác thực password trước khi xóa
        if (!domainService.verifyPassword(account, password)) {
            throw new ForbiddenException("Incorrect password");
        }

        // Soft delete: anonymize user profile
        var user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.anonymize();
        userRepository.save(user);

        // Deactivate account
        account.deactivate();
        accountRepository.save(account);
    }
}