package com.socialapp.domain.shared.valueobject;

// ── PostId ────────────────────────────────────────────────────
class PostId extends StringId {
    private PostId(String v) { super(v); }
    public static PostId of(String v) { return new PostId(v); }
    public static PostId generate() { return new PostId(generateUuid()); }
}

