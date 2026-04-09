package com.socialapp.domain.notification.repository;

import com.socialapp.domain.notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> findByOwnerId(String ownerId, int skip, int limit);
    Optional<Notification> findById(String id);
    void markAsRead(String notificationId);
    void markAllAsRead(String ownerId);
    void deleteById(String id);
    long countUnread(String ownerId);
}