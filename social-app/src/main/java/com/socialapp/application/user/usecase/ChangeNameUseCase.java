package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeNameRequest;
import com.socialapp.application.user.dto.response.UserResponseDtos.MessageResponse;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.service.UserDomainService;
import com.socialapp.domain.user.valueobject.FullName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public class ChangeNameUseCase {

    private final UserRepository    userRepository;
    private final UserDomainService userDomainService;

    public ChangeNameUseCase(UserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    @Transactional
    public MessageResponse execute(String userId, ChangeNameRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Domain enforce restriction
        userDomainService.applyChangeName(user, FullName.of(request.familyName(), request.givenName()));
        userRepository.save(user);

        return new MessageResponse("Name updated successfully");
    }
}