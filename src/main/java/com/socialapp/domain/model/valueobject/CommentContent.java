package com.socialapp.domain.model.valueobject;

import java.util.Objects;

/**
 * Nội dung bình luận. Rule: không rỗng, tối đa 10.000 ký tự.
 */
public final class CommentContent {

    private static final int MAX_LENGTH = 10_000;
    private final String value;

    public CommentContent(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Comment content cannot be blank");
        if (value.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Comment content exceeds " + MAX_LENGTH + " characters");
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof CommentContent)) return false;
        return value.equals(((CommentContent) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}