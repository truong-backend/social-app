package com.socialapp.application.usecase.friendship;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.service.FriendshipDomainService;

public class UnblockUserUseCase {
    private final FriendshipDomainService friendshipDomainService;

    public UnblockUserUseCase(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    public void execute(String blockerId, String targetId) {
        friendshipDomainService.unblockUser(new UserId(blockerId), new UserId(targetId));
    }
}