package com.socialapp.application.usecase.auth;

import com.socialapp.application.dto.request.VerifyEmailRequest;
import com.socialapp.domain.service.AuthDomainService;

public class VerifyEmailUseCase {

    private final AuthDomainService authDomainService;

    public VerifyEmailUseCase(AuthDomainService authDomainService) {
        this.authDomainService = authDomainService;
    }

    public void execute(VerifyEmailRequest req) {
        authDomainService.verifyEmail(req.accountId(), req.code());
    }
}