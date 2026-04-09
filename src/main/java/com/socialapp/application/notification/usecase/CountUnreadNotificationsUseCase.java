package com.socialapp.application.notification.usecase;

import com.socialapp.domain.notification.repository.NotificationRepository;

public class CountUnreadNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public CountUnreadNotificationsUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public long execute(String userId) {
        return notificationRepository.countUnread(userId);
    }
}