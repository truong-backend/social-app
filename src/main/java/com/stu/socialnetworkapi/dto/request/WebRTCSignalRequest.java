package com.stu.socialnetworkapi.dto.request;

public record WebRTCSignalRequest(
        String to,        // username của người nhận
        String type,      // "offer" | "answer" | "candidate" | "reject" | "end"
        String payload    // JSON string: SDP hoặc ICE candidate
) {}