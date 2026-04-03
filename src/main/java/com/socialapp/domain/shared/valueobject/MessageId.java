package com.socialapp.domain.shared.valueobject;

// ── MessageId ─────────────────────────────────────────────────
class MessageId extends StringId {
    private MessageId(String v) {
        super(v);
    }

    public static MessageId of(String v) {
        return new MessageId(v);
    }

    public static MessageId generate() {
        return new MessageId(generateUuid());
    }
}
