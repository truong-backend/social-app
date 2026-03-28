package com.socialapp.infrastructure.persistence.notification;

import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.valueobject.NotificationAction;
import com.socialapp.domain.notification.valueobject.NotificationTarget;
import com.socialapp.domain.notification.valueobject.NotificationTargetType;
import com.socialapp.infrastructure.persistence.notification.neo4j.NotificationNeo4jRepository;
import com.socialapp.infrastructure.persistence.notification.neo4j.NotificationNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationNeo4jRepository neo4j;

    @Override
    public Optional<Notification> findById(String id) {
        return neo4j.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByOwnerId(String ownerId, int skip, int limit) {
        return neo4j.findByOwnerId(ownerId, skip, limit)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countUnreadByOwnerId(String ownerId) {
        return neo4j.countUnreadByOwnerId(ownerId);
    }

    @Override
    public Notification save(Notification notification) {
        return toDomain(neo4j.save(toNode(notification)));
    }

    @Override
    public void deleteById(String id) {
        neo4j.deleteById(id);
    }

    private Notification toDomain(NotificationNode n) {
        return Notification.reconstitute(
                n.getId(), n.getOwnerId(), n.getByUserId(),
                NotificationAction.valueOf(n.getAction()),
                NotificationTarget.of(
                        NotificationTargetType.valueOf(n.getTargetType()),
                        n.getTargetId()
                ),
                Boolean.TRUE.equals(n.getIsRead()),
                n.getSentAt() != null ? LocalDateTime.parse(n.getSentAt()) : LocalDateTime.now()
        );
    }

    private NotificationNode toNode(Notification n) {
        return NotificationNode.builder()
                .id(n.getId())
                .ownerId(n.getOwnerId())
                .byUserId(n.getByUserId())
                .action(n.getAction().name())
                .targetType(n.getTarget().getType().name())
                .targetId(n.getTarget().getTargetId())
                .isRead(n.isRead())
                .sentAt(n.getSentAt().toString())
                .build();
    }
}
