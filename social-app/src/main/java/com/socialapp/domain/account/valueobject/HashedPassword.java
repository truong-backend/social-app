package com.socialapp.domain.account.valueobject;

import java.util.Objects;

/**
 * Value Object: HashedPassword
 * Đảm bảo password luôn ở dạng đã hash, không bao giờ là plain text.
 */
public final class HashedPassword {

    private final String value;

    private HashedPassword(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("HashedPassword must not be blank");
        this.value = value;
    }

    /**
     * Tạo từ giá trị hash đã được mã hóa (từ BCrypt / infrastructure layer)
     */
    public static HashedPassword ofHashed(String hashedValue) {
        return new HashedPassword(hashedValue);
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashedPassword hp)) return false;
        return Objects.equals(value, hp.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}