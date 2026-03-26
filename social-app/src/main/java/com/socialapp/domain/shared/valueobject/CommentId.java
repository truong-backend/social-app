package com.socialapp.domain.shared.valueobject;

// ── CommentId ─────────────────────────────────────────────────
class CommentId extends StringId {
    private CommentId(String v) {
        super(v);
    }

    public static CommentId of(String v) {
        return new CommentId(v);
    }

    public static CommentId generate() {
        return new CommentId(generateUuid());
    }
}
