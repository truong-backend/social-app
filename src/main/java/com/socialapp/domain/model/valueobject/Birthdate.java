package com.socialapp.domain.model.valueobject;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Ngày sinh. Rule: người dùng phải ≥ 16 tuổi.
 */
public final class Birthdate {

    private static final int MIN_AGE = 16;

    private final LocalDate value;

    public Birthdate(LocalDate value) {
        if (value == null) throw new IllegalArgumentException("Birthdate cannot be null");
        int age = Period.between(value, LocalDate.now()).getYears();
        if (age < MIN_AGE)
            throw new IllegalArgumentException("User must be at least " + MIN_AGE + " years old");
        this.value = value;
    }

    public LocalDate getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Birthdate)) return false;
        return value.equals(((Birthdate) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}