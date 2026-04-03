package com.socialapp.infrastructure.security;
import com.socialapp.domain.account.service.AccountDomainService.PasswordMatcher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter cho PasswordMatcher - Implement bằng BCrypt (Spring Security)
 */
@Component
public class BcryptPasswordMatcher implements PasswordMatcher {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordMatcher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}