package com.socialapp.infrastructure.persistence.notification.neo4j;

import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.valueobject.NotificationAction;
import com.socialapp.domain.notification.valueobject.NotificationTarget;
import com.socialapp.domain.notification.valueobject.NotificationTargetType;
import com.socialapp.infrastructure.persistence.notification.neo4j.node.NotificationNode;
import com.socialapp.infrastructure.persistence.notification.neo4j.repository.NotificationNeo4jRepository;
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
        return neo4j.findById(id).map(node -> {
            // byUserId lấy qua relationship BY_USER
            String byUserId = neo4j.findByUserIdByNotifId(node.getId());
            return toDomain(node, byUserId);
        });
    }

    @Override
    public List<Notification> findByOwnerId(String ownerId, int skip, int limit) {
        return neo4j.findByOwnerId(ownerId, skip, limit)
                .stream()
                .map(node -> {
                    String byUserId = neo4j.findByUserIdByNotifId(node.getId());
                    return toDomain(node, byUserId);
                })
                .toList();
    }

    @Override
    public long countUnreadByOwnerId(String ownerId) {
        return neo4j.countUnreadByOwnerId(ownerId);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationNode saved = neo4j.save(toNode(notification));

        // (User)-[:HAS_NOTIFICATION]→(Notification)
        neo4j.linkOwnerToNotification(notification.getOwnerId(), saved.getId());

        // (Notification)-[:BY_USER]→(User)
        neo4j.linkNotificationByUser(saved.getId(), notification.getByUserId());

        return toDomain(saved, notification.getByUserId());
    }

    @Override
    public void deleteById(String id) {
        neo4j.deleteById(id);
    }

    // ── Mapper helpers ───────────────────────────────────────

    private Notification toDomain(NotificationNode n, String byUserId) {
        // ownerId resolve qua HAS_NOTIFICATION (owner → notif), không lưu trong node
        // Để tái tạo domain, ownerId được truyền từ context hoặc query riêng nếu cần
        return Notification.reconstitute(
                n.getId(),
                null,           // ownerId không lưu trong node — set từ caller nếu cần
                byUserId,
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
                .action(n.getAction().name())
                .targetType(n.getTarget().getType().name())
                .targetId(n.getTarget().getTargetId())
                .isRead(n.isRead())
                .sentAt(n.getSentAt().toString())
                .build();
    }
}
