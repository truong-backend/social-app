package com.socialapp.application.relationship.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.entity.FriendRequest;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

record MessageResponse(String message) {}

// ── SendFriendRequestUseCase ─────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class SendFriendRequestUseCase {

    private final UserRepository            userRepository;
    private final FriendRepository          friendRepository;
    private final FriendRequestRepository   friendRequestRepository;
    private final BlockRepository           blockRepository;
    private final RelationshipDomainService domainService;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public MessageResponse execute(String senderId, String receiverId) {

        // Validate target tồn tại
        userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        domainService.validateSendRequest(
                senderId, receiverId,
                friendRepository.existsFriendship(senderId, receiverId),
                friendRequestRepository.exists(senderId, receiverId),
                blockRepository.exists(receiverId, senderId),   // sender bị block bởi receiver
                blockRepository.exists(senderId, receiverId)    // sender đã block receiver
        );

        FriendRequest request = domainService.createRequest(senderId, receiverId);
        friendRequestRepository.save(request);

        // Cập nhật counter
        User sender   = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();
        sender.onRequestSent();
        receiver.onRequestReceived();
        userRepository.save(sender);
        userRepository.save(receiver);

        // Notification
        Notification noti = notificationDomainService
                .createFriendRequestNotification(receiverId, senderId, senderId + "->" + receiverId);
        notificationRepository.save(noti);
        realtimePublisher.publishToUser(receiverId, "NOTIFICATION", noti);

        return new MessageResponse("Friend request sent");
    }
}

// ── AcceptFriendRequestUseCase ───────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class AcceptFriendRequestUseCase {

    private final UserRepository            userRepository;
    private final FriendRepository          friendRepository;
    private final FriendRequestRepository   friendRequestRepository;
    private final RelationshipDomainService domainService;
    private final NotificationRepository    notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher         realtimePublisher;

    @Transactional
    public MessageResponse execute(String receiverId, String senderId) {

        domainService.validateAcceptRequest(
                friendRequestRepository.exists(senderId, receiverId));

        // Xóa request, tạo friendship
        friendRequestRepository.delete(senderId, receiverId);
        FriendRelationship friendship = domainService.createFriendship(senderId, receiverId);
        friendRepository.save(friendship);

        // Cập nhật counter cả 2 phía
        User sender   = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();
        sender.onRequestSentCancelled();
        sender.onFriendAdded();
        receiver.onRequestReceivedHandled();
        receiver.onFriendAdded();
        userRepository.save(sender);
        userRepository.save(receiver);

        // Notify sender
        Notification noti = notificationDomainService
                .createBeFriendNotification(senderId, receiverId, senderId + "-" + receiverId);
        notificationRepository.save(noti);
        realtimePublisher.publishToUser(senderId, "NOTIFICATION", noti);

        return new MessageResponse("Friend request accepted");
    }
}

// ── DeleteFriendRequestUseCase ───────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class DeleteFriendRequestUseCase {

    private final UserRepository            userRepository;
    private final FriendRequestRepository   friendRequestRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String requesterId, String targetId) {

        // Tìm request theo cả 2 chiều (hủy gửi hoặc từ chối nhận)
        boolean sentByMe     = friendRequestRepository.exists(requesterId, targetId);
        boolean receivedByMe = friendRequestRepository.exists(targetId, requesterId);

        domainService.validateDeleteRequest(sentByMe || receivedByMe);

        String actualSender   = sentByMe ? requesterId : targetId;
        String actualReceiver = sentByMe ? targetId : requesterId;
        friendRequestRepository.delete(actualSender, actualReceiver);

        // Cập nhật counter
        User sender   = userRepository.findById(actualSender).orElseThrow();
        User receiver = userRepository.findById(actualReceiver).orElseThrow();
        sender.onRequestSentCancelled();
        receiver.onRequestReceivedHandled();
        userRepository.save(sender);
        userRepository.save(receiver);

        return new MessageResponse("Friend request removed");
    }
}

// ── UnfriendUseCase ──────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UnfriendUseCase {

    private final UserRepository            userRepository;
    private final FriendRepository          friendRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String requesterId, String targetId) {

        domainService.validateUnfriend(
                friendRepository.existsFriendship(requesterId, targetId));

        friendRepository.delete(requesterId, targetId);

        User requester = userRepository.findById(requesterId).orElseThrow();
        User target    = userRepository.findById(targetId).orElseThrow();
        requester.onFriendRemoved();
        target.onFriendRemoved();
        userRepository.save(requester);
        userRepository.save(target);

        return new MessageResponse("Unfriended successfully");
    }
}

// ── BlockUserUseCase ─────────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class BlockUserUseCase {

    private final UserRepository            userRepository;
    private final BlockRepository           blockRepository;
    private final FriendRepository          friendRepository;
    private final FriendRequestRepository   friendRequestRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String blockerId, String blockedId) {

        userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        domainService.validateBlock(blockRepository.exists(blockerId, blockedId));

        // Nếu đang là bạn → tự động unfriend
        if (friendRepository.existsFriendship(blockerId, blockedId)) {
            friendRepository.delete(blockerId, blockedId);
            User blocker = userRepository.findById(blockerId).orElseThrow();
            User blocked = userRepository.findById(blockedId).orElseThrow();
            blocker.onFriendRemoved();
            blocked.onFriendRemoved();
            userRepository.save(blocker);
            userRepository.save(blocked);
        }

        // Xóa pending request nếu có
        if (friendRequestRepository.exists(blockerId, blockedId))
            friendRequestRepository.delete(blockerId, blockedId);
        if (friendRequestRepository.exists(blockedId, blockerId))
            friendRequestRepository.delete(blockedId, blockerId);

        BlockRelationship block = domainService.createBlock(blockerId, blockedId);
        blockRepository.save(block);

        User blocker = userRepository.findById(blockerId).orElseThrow();
        blocker.onUserBlocked();
        userRepository.save(blocker);

        return new MessageResponse("User blocked");
    }
}

// ── UnblockUserUseCase ───────────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UnblockUserUseCase {

    private final UserRepository            userRepository;
    private final BlockRepository           blockRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String blockerId, String blockedId) {

        domainService.validateUnblock(blockRepository.exists(blockerId, blockedId));
        blockRepository.delete(blockerId, blockedId);

        User blocker = userRepository.findById(blockerId).orElseThrow();
        blocker.onUserUnblocked();
        userRepository.save(blocker);

        return new MessageResponse("User unblocked");
    }
}