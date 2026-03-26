package com.socialapp.domain.user.valueobject;

import java.util.Objects;

/**
 * Value Object: FullName (familyName + givenName)
 */
public final class FullName {

    private final String familyName;
    private final String givenName;

    private FullName(String familyName, String givenName) {
        if (familyName == null || familyName.isBlank())
            throw new IllegalArgumentException("FamilyName must not be blank");
        if (givenName == null || givenName.isBlank())
            throw new IllegalArgumentException("GivenName must not be blank");
        this.familyName = familyName.trim();
        this.givenName  = givenName.trim();
    }

    public static FullName of(String familyName, String givenName) {
        return new FullName(familyName, givenName);
    }

    public String getFamilyName() { return familyName; }
    public String getGivenName()  { return givenName; }
    public String getDisplayName() { return familyName + " " + givenName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FullName fn)) return false;
        return Objects.equals(familyName, fn.familyName)
                && Objects.equals(givenName, fn.givenName);
    }

    @Override
    public int hashCode() { return Objects.hash(familyName, givenName); }
}