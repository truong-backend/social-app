package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFriendRequestUseCase {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RelationshipDomainService domainService;

    @Transactional
    public MessageResponse execute(String requesterId, String targetId) {

        // Tìm request theo cả 2 chiều (hủy gửi hoặc từ chối nhận)
        boolean sentByMe = friendRequestRepository.exists(requesterId, targetId);
        boolean receivedByMe = friendRequestRepository.exists(targetId, requesterId);

        domainService.validateDeleteRequest(sentByMe || receivedByMe);

        String actualSender = sentByMe ? requesterId : targetId;
        String actualReceiver = sentByMe ? targetId : requesterId;
        friendRequestRepository.delete(actualSender, actualReceiver);

        // Cập nhật counter
        User sender = userRepository.findById(actualSender).orElseThrow();
        User receiver = userRepository.findById(actualReceiver).orElseThrow();
        sender.onRequestSentCancelled();
        receiver.onRequestReceivedHandled();
        userRepository.save(sender);
        userRepository.save(receiver);

        return new MessageResponse("Friend request removed");
    }
}
