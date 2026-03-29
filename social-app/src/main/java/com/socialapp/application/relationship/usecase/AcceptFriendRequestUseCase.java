package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
import com.socialapp.domain.relationship.entity.FriendRelationship;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public class AcceptFriendRequestUseCase {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RelationshipDomainService domainService;
    private final NotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher realtimePublisher;

    public AcceptFriendRequestUseCase(UserRepository userRepository, FriendRepository friendRepository, FriendRequestRepository friendRequestRepository, RelationshipDomainService domainService, NotificationRepository notificationRepository, NotificationDomainService notificationDomainService, RealtimePublisher realtimePublisher) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.domainService = domainService;
        this.notificationRepository = notificationRepository;
        this.notificationDomainService = notificationDomainService;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public MessageResponse execute(String receiverId, String senderId) {

        domainService.validateAcceptRequest(
                friendRequestRepository.exists(senderId, receiverId));

        // Xóa request, tạo friendship
        friendRequestRepository.delete(senderId, receiverId);
        FriendRelationship friendship = domainService.createFriendship(senderId, receiverId);
        friendRepository.save(friendship);

        // Cập nhật counter cả 2 phía
        User sender = userRepository.findById(senderId).orElseThrow();
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
