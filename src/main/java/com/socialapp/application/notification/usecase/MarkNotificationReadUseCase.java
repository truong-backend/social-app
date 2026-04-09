package com.socialapp.application.notification.usecase;

import com.socialapp.application.shared.exception.ForbiddenException;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.notification.repository.NotificationRepository;

public class MarkNotificationReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkNotificationReadUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void execute(String userId, String notificationId) {
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Not your notification");
        }
        notificationRepository.markAsRead(notificationId);
    }
}