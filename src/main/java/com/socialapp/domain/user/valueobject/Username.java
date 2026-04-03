package com.socialapp.domain.user.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Username
 * Rule: 3-30 ký tự, chỉ chứa chữ, số, dấu gạch dưới
 */
public final class Username {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");

    private final String value;

    private Username(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Username must not be blank");
        if (!PATTERN.matcher(value).matches())
            throw new IllegalArgumentException(
                    "Username must be 3-30 chars, only letters, digits, underscores");
        this.value = value;
    }

    public static Username of(String value) { return new Username(value); }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Username u)) return false;
        return Objects.equals(value, u.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}