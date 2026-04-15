package com.socialapp.application.usecase.notification;

import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.Notification;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;


public class MarkNotificationReadUseCase {

    private final UserRepository userRepository;

    public MarkNotificationReadUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId, String notificationId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getAllNotifications().stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId))
                .markRead();

        userRepository.save(user);
    }

    public void markAllRead(String userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.getAllNotifications().forEach(Notification::markRead);
        userRepository.save(user);
    }
}