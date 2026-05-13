package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryResponse {
    UUID id;
    String mediaUrl;    // full URL qua /v1/files/{id}, null nếu text story
    String mediaType;   // "image" | "video" | "text"
    String caption;
    String bgColor;
    ZonedDateTime createdAt;
    int viewCount;
    boolean isViewed;   // current user đã xem chưa
}