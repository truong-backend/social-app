package com.socialapp.domain.model.valueobject;

import java.util.regex.Pattern;

/**
 * Đại diện mật khẩu thô chưa hash.
 * Rule: ≥8 ký tự, có chữ thường, chữ hoa, số, ký tự đặc biệt.
 */
public final class RawPassword {

    // (?=.*[a-z]) chữ thường | (?=.*[A-Z]) chữ hoa | (?=.*\d) số | (?=.*[\W_]) ký tự đặc biệt
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$");

    private final String value;

    public RawPassword(String value) {
        if (value == null || !PATTERN.matcher(value).matches())
            throw new IllegalArgumentException(
                    "Password must have ≥8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char");
        this.value = value;
    }

    public String getValue() { return value; }
}