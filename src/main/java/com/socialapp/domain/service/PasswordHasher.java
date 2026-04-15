package com.socialapp.domain.service;

import com.socialapp.domain.model.valueobject.HashedPassword;
import com.socialapp.domain.model.valueobject.RawPassword;

/**
 * Domain Port: PasswordHasher
 * Infrastructure sẽ implement bằng BCryptPasswordEncoder.
 * Domain không phụ thuộc vào Spring Security.
 */
public interface PasswordHasher {
    HashedPassword hash(RawPassword rawPassword);
    boolean matches(RawPassword rawPassword, HashedPassword hashedPassword);
}