package com.socialapp.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Username {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{1,32}$");

    private final String value;

    public Username(String value) {
        if (value == null || !PATTERN.matcher(value).matches())
            throw new IllegalArgumentException(
                    "Username must be 1-32 chars, only letters/digits/._-");
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Username)) return false;
        return value.equals(((Username) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public String toString() { return value; }
}