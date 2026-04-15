package com.socialapp.application.usecase.auth;

import com.socialapp.application.dto.request.RegisterRequest;
import com.socialapp.application.dto.response.AccountResponse;
import com.socialapp.application.mapper.AccountMapper;
import com.socialapp.application.port.EmailPort;
import com.socialapp.domain.model.aggregate.Account;
import com.socialapp.domain.model.valueobject.Birthdate;
import com.socialapp.domain.model.valueobject.Email;
import com.socialapp.domain.model.valueobject.RawPassword;
import com.socialapp.domain.service.AuthDomainService;

public class RegisterUseCase {

    private final AuthDomainService authDomainService;
    private final AccountMapper     accountMapper;
    private final EmailPort         emailPort;

    public RegisterUseCase(AuthDomainService authDomainService,
                           AccountMapper accountMapper,
                           EmailPort emailPort) {
        this.authDomainService = authDomainService;
        this.accountMapper     = accountMapper;
        this.emailPort         = emailPort;
    }

    public AccountResponse execute(RegisterRequest req) {
        Account account = authDomainService.register(
                new Email(req.email()),
                new RawPassword(req.password()),
                req.familyName(),
                req.givenName(),
                new Birthdate(req.birthdate())
        );
        // Side-effect: gửi email xác thực (không thuộc domain)
        emailPort.sendVerificationEmail(
                account.getEmail().getValue(),
                account.getVerifyCode().getCode()
        );
        return accountMapper.toResponse(account);
    }
}