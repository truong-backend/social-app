package com.socialapp.application.usecase.friendship;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class RejectFriendRequestUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public RejectFriendRequestUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String requesterId, String rejectorId) {
        friendshipDomainService.rejectFriendRequest(new UserId(requesterId), new UserId(rejectorId));
    }
}