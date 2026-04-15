package com.socialapp.application.dto.response;

import java.time.LocalDateTime;

public record CallResponse(
        String        id,
        String        callId,
        String        callerId,
        boolean       isVideoCall,
        boolean       isAnswered,
        boolean       isEnded,
        LocalDateTime callAt,
        LocalDateTime endAt
) {}