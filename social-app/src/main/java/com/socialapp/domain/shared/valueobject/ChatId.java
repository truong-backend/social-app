package com.socialapp.domain.shared.valueobject;

// ── ChatId ────────────────────────────────────────────────────
class ChatId extends StringId {
    private ChatId(String v) {
        super(v);
    }

    public static ChatId of(String v) {
        return new ChatId(v);
    }

    public static ChatId generate() {
        return new ChatId(generateUuid());
    }
}
