package com.socialapp.domain.shared.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Generic base cho tất cả String-based ID Value Objects.
 * Dùng khi không cần validation logic riêng.
 */
public abstract class StringId {

    private final String value;

    protected StringId(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(getClass().getSimpleName() + " must not be blank");
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    protected static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringId other = (StringId) o;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}