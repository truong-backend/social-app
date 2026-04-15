package com.socialapp.infrastructure.adapter.persistence;

import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.FileEntity;
import com.socialapp.domain.model.entity.Notification;
import com.socialapp.domain.model.valueobject.*;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.FileNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.NotificationNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.node.UserNode;
import com.socialapp.infrastructure.adapter.persistence.neo4j.repository.UserNeo4jRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserNeo4jRepository neo4jRepo;

    public UserRepositoryAdapter(UserNeo4jRepository neo4jRepo) {
        this.neo4jRepo = neo4jRepo;
    }

    // ── Domain → Node ────────────────────────────────────────────────────

    private UserNode toNode(User user) {
        UserNode node = new UserNode(
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getFamilyName(),
                user.getGivenName(),
                user.getBirthdate().getValue(),
                user.getBio(),
                user.getFriendCount(),
                user.getRequestSentCount(),
                user.getRequestReceivedCount(),
                user.getBlockCount(),
                user.getNextChangeNameDate(),
                user.getNextChangeBirthdateDate(),
                user.getNextChangeUsernameDate()
        );

        // @Relationship HAS_PROFILE_PICTURE
        if (user.getProfilePicture() != null) {
            FileMeta meta = user.getProfilePicture().getMeta();
            node.setProfilePicture(new FileNode(
                    meta.getPath(), meta.getName(),
                    meta.getContentType(), meta.getSizeBytes()
            ));
        }

        // @Relationship HAS_NOTIFICATION
        List<NotificationNode> notifNodes = user.getAllNotifications().stream()
                .map(this::toNotifNode)
                .toList();
        node.setNotifications(notifNodes);

        return node;
    }

    private NotificationNode toNotifNode(Notification n) {
        return new NotificationNode(
                n.getId(),
                n.getAction().name(),
                n.isRead(),
                n.getTarget().getTargetType().name(),
                n.getTarget().getTargetId(),
                n.getSentAt(),
                null
        );
    }

    // ── Node → Domain ────────────────────────────────────────────────────

    private User toDomain(UserNode node) {
        User user = new User(
                new UserId(node.getId()),
                node.getFamilyName(),
                node.getGivenName(),
                new Birthdate(node.getBirthdate()),
                new Username(node.getUsername())
        );
        user.updateBio(node.getBio());
        user.setFriendCount(node.getFriendCount());
        user.setRequestSentCount(node.getRequestSentCount());
        user.setRequestReceivedCount(node.getRequestReceivedCount());
        user.setBlockCount(node.getBlockCount());
        user.setNextChangeNameDate(node.getNextChangeNameDate());
        user.setNextChangeBirthdateDate(node.getNextChangeBirthdateDate());
        user.setNextChangeUsernameDate(node.getNextChangeUsernameDate());

        // @Relationship HAS_PROFILE_PICTURE
        if (node.getProfilePicture() != null) {
            FileNode fn = node.getProfilePicture();
            user.setProfilePicture(new FileEntity(
                    new FileMeta(fn.getPath(), fn.getName(),
                            fn.getContentType(), fn.getSizeBytes())
            ));
        }

        // @Relationship HAS_NOTIFICATION
        if (node.getNotifications() != null) {
            node.getNotifications().forEach(nn -> {
                Notification notif = new Notification(
                        nn.getId(),
                        NotificationAction.valueOf(nn.getAction()),
                        new NotificationTarget(
                                NotificationTarget.TargetType.valueOf(nn.getTargetType()),
                                nn.getTargetId()
                        ),
                        nn.getSentAt(),
                        nn.isRead()
                );
                user.addNotification(notif);
            });
        }

        return user;
    }

    // ── Repository impl ──────────────────────────────────────────────────

    @Override
    public Optional<User> findById(UserId id) {
        return neo4jRepo.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return neo4jRepo.findByUsername(username.getValue()).map(this::toDomain);
    }

    /**
     * Tìm kiếm user: dùng @Query Cypher thay vì findAll() + in-memory filter.
     * Tránh full graph scan và N+1.
     */
    @Override
    public List<User> searchByKeyword(String keyword, int limit, int offset) {
        return neo4jRepo.searchByKeyword(keyword, limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void save(User user) {
        neo4jRepo.save(toNode(user));
    }

    @Override
    public void delete(UserId id) {
        neo4jRepo.deleteById(id.getValue());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return neo4jRepo.existsByUsername(username.getValue());
    }

    @Override
    public long countAll() {
        return neo4jRepo.countAll();
    }

    // ── Friendship queries — delegate đến @Query, không load UserNode ────

    /**
     * Paginated query trực tiếp trên graph, không eager-load toàn bộ UserNode.friends.
     */
    @Override
    public List<User> listFriends(UserId userId, int limit, int offset) {
        return neo4jRepo.listFriends(userId.getValue(), limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<User> listSentRequests(UserId userId, int limit, int offset) {
        return neo4jRepo.listSentRequests(userId.getValue(), limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * listReceivedRequests: trước đây dùng findAll() + in-memory filter.
     * Nay dùng @Query traverse ngược (sender)-[:REQUEST]->(me).
     */
    @Override
    public List<User> listReceivedRequests(UserId userId, int limit, int offset) {
        return neo4jRepo.listReceivedRequests(userId.getValue(), limit, offset)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean hasSentRequest(UserId userId, UserId targetId) {
        return neo4jRepo.hasSentRequest(userId.getValue(), targetId.getValue());
    }

    @Override
    public boolean hasReceivedRequest(UserId userId, UserId senderId) {
        return neo4jRepo.hasReceivedRequest(userId.getValue(), senderId.getValue());
    }

    @Override
    public boolean areFriends(UserId userAId, UserId userBId) {
        return neo4jRepo.areFriends(userAId.getValue(), userBId.getValue());
    }

    @Override
    public boolean isBlocked(UserId blockerId, UserId targetId) {
        return neo4jRepo.isBlocked(blockerId.getValue(), targetId.getValue());
    }

    // ── Relationship mutations — Cypher MERGE/DELETE, không load node ────

    /**
     * Tạo REQUEST relationship bằng Cypher MERGE — không load UserNode.
     * Tránh: findById → mutate list → save (eager-load tất cả relationships).
     */
    @Override
    public void createRequestRelationship(UserId senderId, UserId receiverId) {
        neo4jRepo.createRequestRelationship(senderId.getValue(), receiverId.getValue());
    }

    @Override
    public void deleteRequestRelationship(UserId senderId, UserId receiverId) {
        neo4jRepo.deleteRequestRelationship(senderId.getValue(), receiverId.getValue());
    }

    @Override
    public void createFriendRelationship(UserId userAId, UserId userBId) {
        neo4jRepo.createFriendRelationship(userAId.getValue(), userBId.getValue());
    }

    @Override
    public void deleteFriendRelationship(UserId userAId, UserId userBId) {
        neo4jRepo.deleteFriendRelationship(userAId.getValue(), userBId.getValue());
    }

    @Override
    public void createBlockRelationship(UserId blockerId, UserId targetId) {
        neo4jRepo.createBlockRelationship(blockerId.getValue(), targetId.getValue());
    }

    @Override
    public void deleteBlockRelationship(UserId blockerId, UserId targetId) {
        neo4jRepo.deleteBlockRelationship(blockerId.getValue(), targetId.getValue());
    }
}