package com.socialapp.application.usecase.auth;

import com.socialapp.application.dto.request.LoginRequest;
import com.socialapp.application.dto.response.AuthResponse;
import com.socialapp.application.port.TokenPort;
import com.socialapp.domain.model.aggregate.Account;
import com.socialapp.domain.model.valueobject.Email;
import com.socialapp.domain.model.valueobject.RawPassword;
import com.socialapp.domain.service.AuthDomainService;


public class LoginUseCase {

    private final AuthDomainService authDomainService;
    private final TokenPort         tokenPort;

    public LoginUseCase(AuthDomainService authDomainService,
                        TokenPort tokenPort) {
        this.authDomainService = authDomainService;
        this.tokenPort         = tokenPort;
    }

    public AuthResponse execute(LoginRequest req) {
        Account account = authDomainService.login(
                new Email(req.email()),
                new RawPassword(req.password())
        );
        String token = tokenPort.generate(account.getId(), account.getRole().name());
        return new AuthResponse(
                token,
                "Bearer",
                tokenPort.expiresInSeconds(),
                account.getId(),
                account.getId()   // accountId == userId (HAS_INFO relationship 1:1)
        );
    }
}