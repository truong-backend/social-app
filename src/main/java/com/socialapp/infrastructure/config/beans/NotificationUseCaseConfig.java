package com.socialapp.infrastructure.config.beans;

import com.socialapp.application.mapper.NotificationMapper;
import com.socialapp.application.usecase.notification.GetNotificationsUseCase;
import com.socialapp.application.usecase.notification.MarkNotificationReadUseCase;
import com.socialapp.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationUseCaseConfig {

    @Bean
    public GetNotificationsUseCase getNotificationsUseCase(
            UserRepository userRepository,
            NotificationMapper notificationMapper
    ) {
        return new GetNotificationsUseCase(userRepository, notificationMapper);
    }

    @Bean
    public MarkNotificationReadUseCase markNotificationReadUseCase(
            UserRepository userRepository
    ) {
        return new MarkNotificationReadUseCase(userRepository);
    }
}