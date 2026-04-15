package com.socialapp.application.usecase.auth;

import com.socialapp.application.dto.request.ChangePasswordRequest;
import com.socialapp.domain.model.valueobject.RawPassword;
import com.socialapp.domain.service.AuthDomainService;

public class ChangePasswordUseCase {

    private final AuthDomainService authDomainService;

    public ChangePasswordUseCase(AuthDomainService authDomainService) {
        this.authDomainService = authDomainService;
    }

    public void execute(String accountId, ChangePasswordRequest req) {
        authDomainService.changePassword(
                accountId,
                new RawPassword(req.oldPassword()),
                new RawPassword(req.newPassword())
        );
    }
}