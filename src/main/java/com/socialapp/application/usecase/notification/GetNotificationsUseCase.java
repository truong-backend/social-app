package com.socialapp.application.usecase.notification;

import com.socialapp.application.dto.response.NotificationResponse;
import com.socialapp.application.mapper.NotificationMapper;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;

import java.util.List;

public class GetNotificationsUseCase {

    private final UserRepository     userRepository;
    private final NotificationMapper notificationMapper;

    public GetNotificationsUseCase(UserRepository userRepository,
                                   NotificationMapper notificationMapper) {
        this.userRepository     = userRepository;
        this.notificationMapper = notificationMapper;
    }

    public List<NotificationResponse> execute(String userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return user.getAllNotifications()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public List<NotificationResponse> executeUnreadOnly(String userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return user.getUnreadNotifications()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}