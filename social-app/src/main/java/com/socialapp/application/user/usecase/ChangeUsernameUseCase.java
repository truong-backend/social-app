package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeUsernameRequest;
import com.socialapp.application.user.dto.response.UserResponseDtos.MessageResponse;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import com.socialapp.domain.user.service.UserDomainService;
import com.socialapp.domain.user.valueobject.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeUsernameUseCase {

    private final UserRepository    userRepository;
    private final UserDomainService userDomainService;

    @Transactional
    public MessageResponse execute(String userId, ChangeUsernameRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Username newUsername = Username.of(request.username());
        boolean  exists      = userRepository.existsByUsername(newUsername);

        // Domain validate available + enforce restriction
        userDomainService.applyChangeUsername(user, newUsername, exists);
        userRepository.save(user);

        return new MessageResponse("Username updated successfully");
    }
}