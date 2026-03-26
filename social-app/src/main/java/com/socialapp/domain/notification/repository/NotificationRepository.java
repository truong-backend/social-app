package com.socialapp.domain.notification.repository;

import com.socialapp.domain.notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Optional<Notification> findById(String id);

    List<Notification> findByOwnerId(String ownerId, int skip, int limit);

    long countUnreadByOwnerId(String ownerId);

    Notification save(Notification notification);

    void deleteById(String id);
}