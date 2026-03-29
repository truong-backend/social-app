package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.user.dto.request.UserRequestDtos;
import com.socialapp.application.user.dto.response.UserResponseDtos;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class ChangeBioUseCase {

    private final UserRepository userRepository;

    public ChangeBioUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponseDtos.MessageResponse execute(String userId, UserRequestDtos.ChangeBioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.updateBio(request.bio());
        userRepository.save(user);
        return new UserResponseDtos.MessageResponse("Bio updated successfully");
    }
}
