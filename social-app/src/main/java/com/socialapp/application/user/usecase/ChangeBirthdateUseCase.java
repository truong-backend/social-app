package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeBirthdateRequest;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeBioRequest;
import com.socialapp.application.user.dto.response.UserResponseDtos.MessageResponse;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// ── ChangeBirthdateUseCase ──────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
public class ChangeBirthdateUseCase {

    private final UserRepository userRepository;

    @Transactional
    public MessageResponse execute(String userId, ChangeBirthdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.changeBirthdate(LocalDate.parse(request.birthdate()));
        userRepository.save(user);
        return new MessageResponse("Birthdate updated successfully");
    }
}

// ── ChangeBioUseCase ────────────────────────────────────────────────────────

// ── UpdateProfilePictureUseCase ─────────────────────────────────────────────

