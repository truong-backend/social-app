package com.socialapp.application.account.usecase.Register;

import com.socialapp.application.account.dto.request.AccountRequestDtos.RegisterRequest;
import com.socialapp.application.account.dto.response.AccountResponseDtos.RegisterResponse;
import com.socialapp.application.shared.port.EmailSender;
import com.socialapp.domain.account.entity.Account;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.account.service.AccountDomainService;
import com.socialapp.domain.account.valueobject.HashedPassword;
import com.socialapp.domain.account.valueobject.VerifyCode;
import com.socialapp.domain.shared.valueobject.Email;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.valueobject.FullName;
import com.socialapp.domain.user.valueobject.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public class RegisterUseCase {

    private final AccountRepository    accountRepository;
    private final UserRepository       userRepository;
    private final AccountDomainService accountDomainService;
    private final EmailSender          emailSender;
    private final PasswordEncoder      passwordEncoder;

    public RegisterUseCase(AccountRepository accountRepository, UserRepository userRepository, AccountDomainService accountDomainService, EmailSender emailSender, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountDomainService = accountDomainService;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse execute(RegisterRequest request) {

        Email email = Email.of(request.email());

        // 1. Validate email chưa tồn tại
        accountDomainService.validateRegister(email, accountRepository.existsByEmail(email));

        // 2. Tạo User
        User user = User.create(
                Username.of(request.email().split("@")[0] + "_" + System.currentTimeMillis()),
                FullName.of(request.familyName(), request.givenName()),
                LocalDate.parse(request.birthdate())
        );
        userRepository.save(user);

        // 3. Tạo Account
        HashedPassword hashed = HashedPassword.ofHashed(
                passwordEncoder.encode(request.password()));
        Account account = Account.create(email, hashed, user.getId());

        // 4. Tạo VerifyCode và gán vào account
        VerifyCode code = accountDomainService.generateVerifyCode();
        account.assignVerifyCode(code);
        accountRepository.save(account);

        // 5. Gửi email xác thực
        emailSender.sendVerificationEmail(email.getValue(), code.getCode());

        return new RegisterResponse(account.getId(), email.getValue(),
                "Verification email sent. Please check your inbox.");
    }
}