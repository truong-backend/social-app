package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.AccountResponse;
import com.socialapp.domain.model.aggregate.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail().getValue(),
                account.getRole(),
                account.isVerified()
        );
    }
}