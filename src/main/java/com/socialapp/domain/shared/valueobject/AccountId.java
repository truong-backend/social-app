package com.socialapp.domain.shared.valueobject;

// ── AccountId ─────────────────────────────────────────────────
class AccountId extends StringId {
    private AccountId(String v) {
        super(v);
    }

    public static AccountId of(String v) {
        return new AccountId(v);
    }

    public static AccountId generate() {
        return new AccountId(generateUuid());
    }
}
