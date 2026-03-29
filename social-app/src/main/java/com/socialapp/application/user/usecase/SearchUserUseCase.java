package com.socialapp.application.user.usecase;

import com.socialapp.application.user.dto.response.UserResponseDtos.UserSummaryResponse;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public class SearchUserUseCase {

    private final UserRepository userRepository;

    public SearchUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> execute(String keyword, String requesterId) {
        return userRepository.searchByKeyword(keyword, requesterId)
                .stream()
                .map(u -> new UserSummaryResponse(
                        u.getId(),
                        u.getUsername().getValue(),
                        u.getFullName().getFamilyName(),
                        u.getFullName().getGivenName(),
                        u.getProfilePicturePath()
                ))
                .toList();
    }
}