package com.socialapp.domain.user.valueobject;

public class FullName {

    private final String familyName;
    private final String givenName;

    private FullName(String familyName, String givenName) {
        this.familyName = familyName;
        this.givenName  = givenName;
    }

    public static FullName of(String familyName, String givenName) {
        return new FullName(familyName, givenName);
    }

    public String getFamilyName() { return familyName; }
    public String getGivenName()  { return givenName; }

    @Override
    public String toString() {
        return (familyName != null ? familyName : "") + " " +
                (givenName  != null ? givenName  : "");
    }
}