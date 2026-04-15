package com.socialapp.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class HashedPassword {

    private final String value;

    public HashedPassword(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("HashedPassword cannot be blank");
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashedPassword)) return false;
        return value.equals(((HashedPassword) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}