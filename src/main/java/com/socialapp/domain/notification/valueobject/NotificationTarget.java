package com.socialapp.domain.notification.valueobject;

import java.util.Objects;

/**
 * Value Object: NotificationTarget
 * Gom targetType + targetId để tránh primitive obsession.
 */
public final class NotificationTarget {

    private final NotificationTargetType type;
    private final String targetId;

    public NotificationTarget(NotificationTargetType type, String targetId) {
        if (type == null)
            throw new IllegalArgumentException("NotificationTargetType must not be null");
        if (targetId == null || targetId.isBlank())
            throw new IllegalArgumentException("TargetId must not be blank");
        this.type     = type;
        this.targetId = targetId;
    }

    public static NotificationTarget of(NotificationTargetType type, String targetId) {
        return new NotificationTarget(type, targetId);
    }

    public NotificationTargetType getType()  { return type; }
    public String getTargetId()              { return targetId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationTarget nt)) return false;
        return type == nt.type && Objects.equals(targetId, nt.targetId);
    }

    @Override
    public int hashCode() { return Objects.hash(type, targetId); }
}