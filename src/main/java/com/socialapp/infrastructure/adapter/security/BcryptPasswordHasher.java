package com.socialapp.infrastructure.adapter.security;

import com.socialapp.domain.model.valueobject.HashedPassword;
import com.socialapp.domain.model.valueobject.RawPassword;
import com.socialapp.domain.service.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public BcryptPasswordHasher() {
        // strength 12 — cân bằng giữa bảo mật và tốc độ
        this.encoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public HashedPassword hash(RawPassword rawPassword) {
        String hashed = encoder.encode(rawPassword.getValue());
        return new HashedPassword(hashed);
    }

    @Override
    public boolean matches(RawPassword rawPassword, HashedPassword hashedPassword) {
        return encoder.matches(rawPassword.getValue(), hashedPassword.getValue());
    }
}