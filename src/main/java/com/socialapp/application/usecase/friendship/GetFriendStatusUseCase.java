package com.socialapp.application.usecase.friendship;

import com.socialapp.domain.model.valueobject.UserId;
import com.socialapp.domain.repository.UserRepository;

public class GetFriendStatusUseCase {

    private final UserRepository userRepository;

    public GetFriendStatusUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Trả về trạng thái quan hệ giữa currentUser và targetUser.
     * Values: "none" | "friends" | "request_sent" | "request_received" | "blocked"
     */
    public String execute(String currentUserId, String targetUserId) {
        UserId me     = new UserId(currentUserId);
        UserId target = new UserId(targetUserId);

        if (userRepository.isBlocked(me, target) || userRepository.isBlocked(target, me))
            return "blocked";
        if (userRepository.areFriends(me, target))
            return "friends";
        if (userRepository.hasSentRequest(me, target))
            return "request_sent";
        if (userRepository.hasReceivedRequest(me, target))
            return "request_received";
        return "none";
    }
}