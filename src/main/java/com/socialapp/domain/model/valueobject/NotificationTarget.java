package com.socialapp.domain.model.valueobject;

import java.util.Objects;

/**
 * Đối tượng mà notification trỏ đến (REQUEST, FRIEND, POST, COMMENT…).
 */
public final class NotificationTarget {

    public enum TargetType { REQUEST, FRIEND, POST, COMMENT }

    private final TargetType targetType;
    private final String     targetId;

    public NotificationTarget(TargetType targetType, String targetId) {
        if (targetType == null) throw new IllegalArgumentException("TargetType is required");
        if (targetId == null || targetId.isBlank())
            throw new IllegalArgumentException("TargetId cannot be blank");
        this.targetType = targetType;
        this.targetId   = targetId;
    }

    public TargetType getTargetType() { return targetType; }
    public String     getTargetId()   { return targetId; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof NotificationTarget)) return false;
        NotificationTarget that = (NotificationTarget) o;
        return targetType == that.targetType && targetId.equals(that.targetId);
    }

    @Override public int hashCode() { return Objects.hash(targetType, targetId); }
}