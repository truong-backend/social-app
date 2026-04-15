package com.socialapp.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String value;

    public Email(String value) {
        if (value == null || !PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("Invalid email format: " + value);
        this.value = value.toLowerCase();
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        return value.equals(((Email) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public String toString() { return value; }
}