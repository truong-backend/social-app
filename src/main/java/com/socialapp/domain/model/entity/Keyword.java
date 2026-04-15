package com.socialapp.domain.model.entity;

import java.util.Objects;

/**
 * Entity: Keyword
 * Identity: text (unique)
 * Thuộc Aggregate Post — trích xuất từ nội dung bài viết.
 */
public class Keyword {

    private final String text;

    public Keyword(String text) {
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("Keyword text cannot be blank");
        this.text = text.toLowerCase().trim();
    }

    public String getText() { return text; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Keyword)) return false;
        return text.equals(((Keyword) o).text);
    }
    @Override public int hashCode() { return Objects.hash(text); }
}