package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.user.dto.response.UserResponseDtos.UserSummaryResponse;
import com.socialapp.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SearchUserUseCase {

    private final UserRepository userRepository;
    private final FileStorage    fileStorage;

    public SearchUserUseCase(UserRepository userRepository, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.fileStorage    = fileStorage;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> execute(String keyword, String requesterId) {
        return userRepository.searchByKeyword(keyword, requesterId)
                .stream()
                .map(u -> {
                    String rawPath   = u.getProfilePicturePath();
                    String pictureUrl = (rawPath != null && !rawPath.isBlank())
                            ? fileStorage.getPublicUrl(rawPath)
                            : null;
                    return new UserSummaryResponse(
                            u.getId(),
                            u.getUsername().getValue(),
                            u.getFullName().getFamilyName(),
                            u.getFullName().getGivenName(),
                            pictureUrl       // ← URL đầy đủ
                    );
                })
                .toList();
    }
}
