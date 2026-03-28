package com.socialapp.application.notification.usecase;

//import com.socialapp.application.notification.dto.response.NotificationResponse;
import com.socialapp.application.notification.dto.response.NotificationResponse;
import com.socialapp.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> execute(String ownerId, int skip, int limit) {
        return notificationRepository.findByOwnerId(ownerId, skip, limit)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getByUserId(),
                        n.getAction().name(),
                        n.getTarget().getType().name(),
                        n.getTarget().getTargetId(),
                        n.isRead(),
                        n.getSentAt()
                ))
                .toList();
    }
}