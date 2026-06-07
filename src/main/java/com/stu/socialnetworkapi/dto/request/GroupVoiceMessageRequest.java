package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.validation.annotation.ValidVoice;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record GroupVoiceMessageRequest(
        @NotNull(message = "FILE_MESSAGE_REQUIRED")
        @ValidVoice
        MultipartFile voiceFile
) {
}