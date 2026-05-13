package com.stu.socialnetworkapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryGroupResponse {
    UUID userId;
    String username;
    String displayName;
    String avatar;          // full URL profilePicture
    boolean hasNewStory;    // có story chưa xem không
    List<StoryResponse> stories;
}