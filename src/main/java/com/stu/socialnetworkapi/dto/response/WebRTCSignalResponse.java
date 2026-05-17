package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;

@Builder
public record WebRTCSignalResponse(
        String from,      // username người gửi
        String type,      // "offer" | "answer" | "candidate" | "reject" | "end"
        String payload,   // JSON string: SDP hoặc ICE candidate
        boolean isVideoCall
) {}