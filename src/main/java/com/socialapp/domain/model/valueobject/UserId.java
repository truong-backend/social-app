package com.socialapp.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class UserId {

    private final String value;

    public UserId(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("UserId cannot be blank");
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId)) return false;
        return value.equals(((UserId) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public String toString() { return value; }
}