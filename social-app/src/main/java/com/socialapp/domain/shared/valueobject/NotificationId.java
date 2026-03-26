package com.socialapp.domain.shared.valueobject;

// ── NotificationId ────────────────────────────────────────────
class NotificationId extends StringId {
    private NotificationId(String v) {
        super(v);
    }

    public static NotificationId of(String v) {
        return new NotificationId(v);
    }

    public static NotificationId generate() {
        return new NotificationId(generateUuid());
    }
}
