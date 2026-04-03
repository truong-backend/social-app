package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.domain.relationship.entity.BlockRelationship;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.repository.FriendRepository;
import com.socialapp.domain.relationship.repository.FriendRequestRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public class BlockUserUseCase {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RelationshipDomainService domainService;

    public BlockUserUseCase(UserRepository userRepository, BlockRepository blockRepository, FriendRepository friendRepository, FriendRequestRepository friendRequestRepository, RelationshipDomainService domainService) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.domainService = domainService;
    }

    @Transactional
    public MessageResponse execute(String blockerId, String blockedId) {

        userRepository.findById(blockedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        domainService.validateBlock(blockRepository.exists(blockerId, blockedId));

        // Nếu đang là bạn → tự động unfriend
        if (friendRepository.existsFriendship(blockerId, blockedId)) {
            friendRepository.delete(blockerId, blockedId);
            User blocker = userRepository.findById(blockerId).orElseThrow();
            User blocked = userRepository.findById(blockedId).orElseThrow();
            blocker.onFriendRemoved();
            blocked.onFriendRemoved();
            userRepository.save(blocker);
            userRepository.save(blocked);
        }

        // Xóa pending request nếu có
        if (friendRequestRepository.exists(blockerId, blockedId))
            friendRequestRepository.delete(blockerId, blockedId);
        if (friendRequestRepository.exists(blockedId, blockerId))
            friendRequestRepository.delete(blockedId, blockerId);

        BlockRelationship block = domainService.createBlock(blockerId, blockedId);
        blockRepository.save(block);

        User blocker = userRepository.findById(blockerId).orElseThrow();
        blocker.onUserBlocked();
        userRepository.save(blocker);

        return new MessageResponse("User blocked");
    }
}
