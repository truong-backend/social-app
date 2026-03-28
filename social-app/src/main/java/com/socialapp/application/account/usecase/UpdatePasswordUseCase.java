package com.socialapp.application.account.usecase;

import com.socialapp.application.account.dto.request.AccountRequestDtos.UpdatePasswordRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.exception.AccountDomainException;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.valueobject.HashedPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePasswordUseCase {

    private final AccountRepository accountRepository;
    private final PasswordEncoder   passwordEncoder;

    @Transactional
    public MessageResponse execute(String accountId, UpdatePasswordRequest request) {

        if (!request.newPassword().equals(request.confirmPassword()))
            throw new AccountDomainException("Passwords do not match");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        HashedPassword hashed = HashedPassword.ofHashed(
                passwordEncoder.encode(request.newPassword()));
        account.changePassword(hashed);
        accountRepository.save(account);

        return new MessageResponse("Password updated successfully");
    }
}