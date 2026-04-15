package com.socialapp.application.mapper;

import com.socialapp.application.dto.response.NotificationResponse;
import com.socialapp.domain.model.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAction().name(),
                notification.getTarget().getTargetType().name(),
                notification.getTarget().getTargetId(),
                notification.isRead(),
                notification.getSentAt()
        );
    }
}