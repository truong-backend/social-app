package com.socialapp.application.account.usecase;

import com.socialapp.application.account.dto.request.AccountRequestDtos.PrepareResetPasswordRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.EmailSender;
import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrepareResetPasswordUseCase {

    private final AccountRepository    accountRepository;
    private final AccountDomainService accountDomainService;
    private final EmailSender          emailSender;

    @Transactional
    public MessageResponse execute(PrepareResetPasswordRequest request) {

        Account account = accountRepository.findByEmail(Email.of(request.email()))
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        VerifyCode code = accountDomainService.generateVerifyCode();
        account.assignVerifyCode(code);
        accountRepository.save(account);

        emailSender.sendPasswordResetEmail(request.email(), code.getCode());

        return new MessageResponse("Reset code sent to your email");
    }
}