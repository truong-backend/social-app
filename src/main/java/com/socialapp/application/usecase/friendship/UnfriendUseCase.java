package com.socialapp.application.usecase.friendship;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class UnfriendUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public UnfriendUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String userAId, String userBId) {
        friendshipDomainService.unfriend(new UserId(userAId), new UserId(userBId));
    }
}