package com.socialapp.application.notification.usecase;

import com.socialapp.domain.notification.repository.NotificationRepository;

public class MarkAllNotificationsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkAllNotificationsReadUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void execute(String userId) {
        notificationRepository.markAllAsRead(userId);
    }
}