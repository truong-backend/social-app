package com.socialapp.domain.notification.entity;

import com.socialapp.domain.notification.exception.NotificationDomainException;
import com.socialapp.domain.notification.valueobject.NotificationAction;
import com.socialapp.domain.notification.valueobject.NotificationTarget;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity: Notification
 *
 * Chịu trách nhiệm:
 *  - Lưu thông tin thông báo gửi đến người dùng
 *  - Quản lý trạng thái đã đọc / chưa đọc
 *  - byUserId: người tạo ra hành động dẫn đến thông báo
 *  - ownerId:  người nhận thông báo
 */
public class Notification {

    // ── Identity ──────────────────────────────────────────────
    private final String id;
    private final String ownerId;   // người nhận
    private final String byUserId;  // người tạo hành động

    // ── Value Objects ─────────────────────────────────────────
    private final NotificationAction action;
    private final NotificationTarget target;

    // ── State ─────────────────────────────────────────────────
    private boolean isRead;

    // ── Timestamp ─────────────────────────────────────────────
    private final LocalDateTime sentAt;

    // ── Private constructor ───────────────────────────────────
    private Notification(String id, String ownerId, String byUserId,
                         NotificationAction action, NotificationTarget target,
                         boolean isRead, LocalDateTime sentAt) {
        this.id       = id;
        this.ownerId  = ownerId;
        this.byUserId = byUserId;
        this.action   = action;
        this.target   = target;
        this.isRead   = isRead;
        this.sentAt   = sentAt;
    }

    // ── Factory Methods ───────────────────────────────────────

    public static Notification create(String ownerId, String byUserId,
                                      NotificationAction action,
                                      NotificationTarget target) {
        if (ownerId.equals(byUserId))
            throw new NotificationDomainException(
                    "Cannot send notification to yourself");
        return new Notification(
                UUID.randomUUID().toString(),
                ownerId, byUserId,
                action, target,
                false,
                LocalDateTime.now()
        );
    }

    public static Notification reconstitute(String id, String ownerId, String byUserId,
                                            NotificationAction action, NotificationTarget target,
                                            boolean isRead, LocalDateTime sentAt) {
        return new Notification(id, ownerId, byUserId, action, target, isRead, sentAt);
    }

    // ── Domain Behaviors ──────────────────────────────────────

    public void markAsRead() {
        if (isRead)
            throw new NotificationDomainException("Notification already marked as read");
        this.isRead = true;
    }

    // ── Getters ───────────────────────────────────────────────

    public String getId()                   { return id; }
    public String getOwnerId()              { return ownerId; }
    public String getByUserId()             { return byUserId; }
    public NotificationAction getAction()   { return action; }
    public NotificationTarget getTarget()   { return target; }
    public boolean isRead()                 { return isRead; }
    public LocalDateTime getSentAt()        { return sentAt; }
}