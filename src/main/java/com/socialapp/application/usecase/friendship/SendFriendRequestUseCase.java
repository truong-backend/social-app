package com.socialapp.application.usecase.friendship;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class SendFriendRequestUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public SendFriendRequestUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String senderId, String receiverId) {
        friendshipDomainService.sendFriendRequest(new UserId(senderId), new UserId(receiverId));
    }
}