package com.socialapp.infrastructure.persistence.notification.neo4j;

import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.valueobject.NotificationAction;
import com.socialapp.domain.notification.valueobject.NotificationTarget;
import com.socialapp.domain.notification.valueobject.NotificationTargetType;
import com.socialapp.infrastructure.persistence.notification.neo4j.node.NotificationNode;
import com.socialapp.infrastructure.persistence.notification.neo4j.repository.NotificationNeo4jRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationNeo4jRepository neo4jRepository;

    @Override
    public void save(Notification notification) {
        neo4jRepository.save(toNode(notification));
    }

    @Override
    public List<Notification> findByOwnerId(String ownerId, int skip, int limit) {
        return neo4jRepository.findByOwnerIdOrderBySentAtDesc(ownerId, skip, limit)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Notification> findById(String id) {
        return neo4jRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void markAsRead(String notificationId) {
        neo4jRepository.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(String ownerId) {
        neo4jRepository.markAllAsRead(ownerId);
    }

    @Override
    public void deleteById(String id) {
        neo4jRepository.deleteById(id);
    }

    @Override
    public long countUnread(String ownerId) {
        return neo4jRepository.countUnreadByOwnerId(ownerId);
    }

    private Notification toDomain(NotificationNode node) {
        return Notification.reconstitute(
                node.getId(),
                node.getOwnerId(),
                node.getByUserId(),
                NotificationAction.valueOf(node.getAction()),
                NotificationTarget.of(
                        NotificationTargetType.valueOf(node.getTargetType()),
                        node.getTargetId()
                ),
                node.isRead(),
                node.getSentAt()
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
                .sentAt(n.getSentAt())
                .build();
    }
}