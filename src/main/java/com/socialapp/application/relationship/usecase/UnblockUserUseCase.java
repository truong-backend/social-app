package com.socialapp.application.relationship.usecase;

import com.socialapp.application.relationship.dto.response.MessageResponse;
import com.socialapp.domain.relationship.repository.BlockRepository;
import com.socialapp.domain.relationship.service.RelationshipDomainService;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class UnblockUserUseCase {

    private final UserRepository            userRepository;
    private final BlockRepository           blockRepository;
    private final RelationshipDomainService domainService;

    public UnblockUserUseCase(UserRepository userRepository, BlockRepository blockRepository, RelationshipDomainService domainService) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.domainService = domainService;
    }

    @Transactional
    public MessageResponse execute(String blockerId, String blockedId) {

        domainService.validateUnblock(blockRepository.exists(blockerId, blockedId));
        blockRepository.delete(blockerId, blockedId);

        User blocker = userRepository.findById(blockerId).orElseThrow();
        blocker.onUserUnblocked();
        userRepository.save(blocker);

        return new MessageResponse("User unblocked");
    }
}