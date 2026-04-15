package com.socialapp.application.usecase.friendship;


import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class BlockUserUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public BlockUserUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String blockerId, String targetId) {
        friendshipDomainService.blockUser(new UserId(blockerId), new UserId(targetId));
    }
}