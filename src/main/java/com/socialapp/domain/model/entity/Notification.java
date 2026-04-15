package com.socialapp.domain.model.entity;

import com.socialapp.domain.model.valueobject.NotificationAction;
import com.socialapp.domain.model.valueobject.NotificationTarget;

import java.time.LocalDateTime;

/**
 * Entity: Notification
 * Identity: id
 * Thuộc Aggregate User.
 */
public class Notification {

    private final String               id;
    private final NotificationAction   action;
    private final NotificationTarget   target;
    private final LocalDateTime        sentAt;
    private       boolean              isRead;

    public Notification(String id,
                        NotificationAction action,
                        NotificationTarget target) {
        this.id     = id;
        this.action = action;
        this.target = target;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    /** Constructor load từ DB */
    public Notification(String id,
                        NotificationAction action,
                        NotificationTarget target,
                        LocalDateTime sentAt,
                        boolean isRead) {
        this.id     = id;
        this.action = action;
        this.target = target;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    public void markRead() { this.isRead = true; }

    public String             getId()     { return id; }
    public NotificationAction getAction() { return action; }
    public NotificationTarget getTarget() { return target; }
    public LocalDateTime      getSentAt() { return sentAt; }
    public boolean            isRead()    { return isRead; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Notification)) return false;
        return id.equals(((Notification) o).id);
    }

    @Override public int hashCode() { return id.hashCode(); }
}