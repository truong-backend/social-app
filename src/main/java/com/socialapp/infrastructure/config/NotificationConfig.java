package com.socialapp.infrastructure.config;

import com.socialapp.application.notification.usecase.*;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.infrastructure.persistence.notification.neo4j.NotificationRepositoryAdapter;
import com.socialapp.infrastructure.persistence.notification.neo4j.repository.NotificationNeo4jRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Bean
    public NotificationRepository notificationRepository(
            NotificationNeo4jRepository neo4jRepository) {
        return new NotificationRepositoryAdapter(neo4jRepository);
    }

    @Bean
    public GetNotificationsUseCase getNotificationsUseCase(
            NotificationRepository notificationRepository) {
        return new GetNotificationsUseCase(notificationRepository);
    }

    @Bean
    public MarkNotificationReadUseCase markNotificationReadUseCase(
            NotificationRepository notificationRepository) {
        return new MarkNotificationReadUseCase(notificationRepository);
    }

    @Bean
    public MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase(
            NotificationRepository notificationRepository) {
        return new MarkAllNotificationsReadUseCase(notificationRepository);
    }

    @Bean
    public DeleteNotificationUseCase deleteNotificationUseCase(
            NotificationRepository notificationRepository) {
        return new DeleteNotificationUseCase(notificationRepository);
    }

    @Bean
    public CountUnreadNotificationsUseCase countUnreadNotificationsUseCase(
            NotificationRepository notificationRepository) {
        return new CountUnreadNotificationsUseCase(notificationRepository);
    }
}