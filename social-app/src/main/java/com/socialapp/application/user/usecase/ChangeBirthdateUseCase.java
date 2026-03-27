package com.socialapp.application.user.usecase;

import com.socialapp.application.shared.exception.ResourceNotFoundException;
import com.socialapp.application.shared.port.FileStorage;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeBirthdateRequest;
import com.socialapp.application.user.dto.request.UserRequestDtos.ChangeBioRequest;
import com.socialapp.application.user.dto.response.UserResponseDtos.MessageResponse;
import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.domain.user.entity.User;
import com.socialapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

// ── ChangeBirthdateUseCase ──────────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class ChangeBirthdateUseCase {

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

@Service
@RequiredArgsConstructor
class ChangeBioUseCase {

    private final UserRepository userRepository;

    @Transactional
    public MessageResponse execute(String userId, ChangeBioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.updateBio(request.bio());
        userRepository.save(user);
        return new MessageResponse("Bio updated successfully");
    }
}

// ── UpdateProfilePictureUseCase ─────────────────────────────────────────────

@Service
@RequiredArgsConstructor
class UpdateProfilePictureUseCase {

    private final UserRepository userRepository;
    private final FileStorage    fileStorage;
    private final FileRepository fileRepository;

    @Transactional
    public MessageResponse execute(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Xóa ảnh cũ nếu có
        if (user.getProfilePicturePath() != null) {
            fileStorage.delete(user.getProfilePicturePath());
            fileRepository.deleteByPath(user.getProfilePicturePath());
        }

        // Upload ảnh mới
        String path     = fileStorage.upload(file);
        FileNode fileNode = FileNode.create(path, file.getOriginalFilename(),
                file.getContentType());
        fileRepository.save(fileNode);

        user.updateProfilePicture(path);
        userRepository.save(user);

        return new MessageResponse("Profile picture updated successfully");
    }
}