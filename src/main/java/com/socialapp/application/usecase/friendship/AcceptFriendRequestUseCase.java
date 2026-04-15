package com.socialapp.application.usecase.friendship;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class AcceptFriendRequestUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public AcceptFriendRequestUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String requesterId, String acceptorId) {
        friendshipDomainService.acceptFriendRequest(new UserId(requesterId), new UserId(acceptorId));
    }
}