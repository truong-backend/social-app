package com.socialapp.application.usecase.friendship;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class CancelFriendRequestUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public CancelFriendRequestUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String senderId, String receiverId) {
        friendshipDomainService.cancelFriendRequest(new UserId(senderId), new UserId(receiverId));
    }
}