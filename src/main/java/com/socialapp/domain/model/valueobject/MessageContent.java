package com.socialapp.domain.model.valueobject;

import java.util.Objects;

/**
 * Nội dung tin nhắn. Rule: tối đa 10.000 ký tự (có thể rỗng nếu chỉ gửi file).
 */
public final class MessageContent {

    private static final int MAX_LENGTH = 10_000;
    private final String value;

    public MessageContent(String value) {
        this.value = (value == null) ? "" : value;
        if (this.value.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Message content exceeds " + MAX_LENGTH + " characters");
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof MessageContent)) return false;
        return value.equals(((MessageContent) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
}