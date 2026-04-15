package com.socialapp.domain.service;

import com.socialapp.application.dto.response.ErrorCode;
import com.socialapp.domain.model.aggregate.User;
import com.socialapp.domain.model.entity.Notification;
import com.socialapp.domain.model.valueobject.NotificationAction;
import com.socialapp.domain.model.valueobject.NotificationTarget;
import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;
import com.socialapp.presentation.advice.DomainException;

import java.util.UUID;

/**
 * Domain Service: FriendshipDomainService
 * ─────────────────────────────────────────────────────────────
 * Xử lý kết bạn — logic liên quan đến 2 User Aggregate.
 *
 * Trách nhiệm:
 *   - Gửi / hủy lời mời kết bạn
 *   - Chấp nhận / từ chối lời mời
 *   - Hủy kết bạn
 *   - Block / unblock
 *   - Gợi ý kết bạn (delegate lên Application layer)
 *
 * Rules được enforce:
 *   - Không tự kết bạn với chính mình
 *   - Giới hạn 100 lời mời gửi / nhận
 *   - Giới hạn 100 bạn bè
 *   - Giới hạn 100 block
 */
public class FriendshipDomainService {

    private final UserRepository userRepository;

    public FriendshipDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── Send Request ─────────────────────────────────────────

    public void sendFriendRequest(UserId senderId, UserId receiverId) {
        if (senderId.equals(receiverId))
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Cannot send friend request to yourself");

        User sender   = requireUser(senderId);
        User receiver = requireUser(receiverId);

        sender.sendFriendRequest();
        receiver.receiveRequest();

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                NotificationAction.SENT_ADD_FRIEND_REQUEST,
                new NotificationTarget(NotificationTarget.TargetType.REQUEST, senderId.getValue())
        );
        receiver.addNotification(notification);

        userRepository.save(sender);
        userRepository.save(receiver);
        userRepository.createRequestRelationship(senderId, receiverId);
    }

    // ── Cancel Sent Request ───────────────────────────────────

    public void cancelFriendRequest(UserId senderId, UserId receiverId) {
        User sender   = requireUser(senderId);
        User receiver = requireUser(receiverId);

        sender.cancelSentRequest();
        receiver.cancelReceivedRequest();

        userRepository.save(sender);
        userRepository.save(receiver);
        userRepository.deleteRequestRelationship(senderId, receiverId);
    }

    // ── Accept Request ────────────────────────────────────────

    public void acceptFriendRequest(UserId requesterId, UserId acceptorId) {
        User requester = requireUser(requesterId);
        User acceptor  = requireUser(acceptorId);

        requester.addFriend();
        acceptor.addFriend();
        requester.cancelSentRequest();
        acceptor.cancelReceivedRequest();

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                NotificationAction.BE_FRIEND,
                new NotificationTarget(NotificationTarget.TargetType.FRIEND, acceptorId.getValue())
        );
        requester.addNotification(notification);

        userRepository.save(requester);
        userRepository.save(acceptor);
        userRepository.deleteRequestRelationship(requesterId, acceptorId);
        userRepository.createFriendRelationship(requesterId, acceptorId);
    }

    // ── Reject Request ────────────────────────────────────────

    public void rejectFriendRequest(UserId requesterId, UserId rejectorId) {
        User requester = requireUser(requesterId);
        User rejector  = requireUser(rejectorId);

        requester.cancelSentRequest();
        rejector.cancelReceivedRequest();

        userRepository.save(requester);
        userRepository.save(rejector);
        userRepository.deleteRequestRelationship(requesterId, rejectorId);
    }

    // ── Unfriend ──────────────────────────────────────────────

    public void unfriend(UserId userAId, UserId userBId) {
        User userA = requireUser(userAId);
        User userB = requireUser(userBId);

        userA.removeFriend();
        userB.removeFriend();

        userRepository.save(userA);
        userRepository.save(userB);
        userRepository.deleteFriendRelationship(userAId, userBId);
    }

    // ── Block / Unblock ───────────────────────────────────────

    public void blockUser(UserId blockerId, UserId targetId) {
        if (blockerId.equals(targetId))
            throw new DomainException(ErrorCode.FORBIDDEN, "Cannot block yourself");

        User blocker = requireUser(blockerId);
        blocker.blockUser();
        userRepository.save(blocker);
        userRepository.createBlockRelationship(blockerId, targetId);
    }

    public void unblockUser(UserId blockerId, UserId targetId) {
        User blocker = requireUser(blockerId);
        blocker.unblockUser();
        userRepository.save(blocker);
        userRepository.deleteBlockRelationship(blockerId, targetId);
    }

    // ── Helper ───────────────────────────────────────────────

    private User requireUser(UserId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(
                        ErrorCode.USER_NOT_FOUND, "User not found: " + userId));
    }
}