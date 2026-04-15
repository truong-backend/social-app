package com.socialapp.application.usecase.auth;

import com.socialapp.application.port.EmailPort;
import com.socialapp.domain.model.entity.VerifyCode;
import com.socialapp.domain.repository.AccountRepository;
import com.socialapp.domain.service.AuthDomainService;

public class ResendVerifyCodeUseCase {

    private final AuthDomainService  authDomainService;
    private final AccountRepository  accountRepository;
    private final EmailPort          emailPort;

    public ResendVerifyCodeUseCase(AuthDomainService authDomainService,
                                   AccountRepository accountRepository,
                                   EmailPort emailPort) {
        this.authDomainService = authDomainService;
        this.accountRepository = accountRepository;
        this.emailPort         = emailPort;
    }

    public void execute(String accountId) {
        VerifyCode newCode = authDomainService.resendVerifyCode(accountId);

        // Lấy email để gửi
        accountRepository.findById(accountId).ifPresent(account ->
                emailPort.sendVerificationEmail(
                        account.getEmail().getValue(),
                        newCode.getCode()
                )
        );
    }
}