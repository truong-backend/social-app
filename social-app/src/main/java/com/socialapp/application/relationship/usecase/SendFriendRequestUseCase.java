package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.RealtimePublisher;
import com.socialapp.domain.notification.entity.Notification;
import com.socialapp.domain.notification.repository.NotificationRepository;
import com.socialapp.domain.notification.service.NotificationDomainService;
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


public class SendFriendRequestUseCase {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final BlockRepository blockRepository;
    private final RelationshipDomainService domainService;
    private final NotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;
    private final RealtimePublisher realtimePublisher;

    public SendFriendRequestUseCase(UserRepository userRepository, FriendRepository friendRepository, FriendRequestRepository friendRequestRepository, BlockRepository blockRepository, RelationshipDomainService domainService, NotificationRepository notificationRepository, NotificationDomainService notificationDomainService, RealtimePublisher realtimePublisher) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.blockRepository = blockRepository;
        this.domainService = domainService;
        this.notificationRepository = notificationRepository;
        this.notificationDomainService = notificationDomainService;
        this.realtimePublisher = realtimePublisher;
    }

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
        User sender = userRepository.findById(senderId).orElseThrow();
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
