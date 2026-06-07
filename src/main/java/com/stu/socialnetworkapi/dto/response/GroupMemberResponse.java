package com.stu.socialnetworkapi.dto.response;

import com.stu.socialnetworkapi.enums.GroupRole;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class GroupMemberResponse {
    String userId;
    String username;
    String givenName;
    String familyName;
    String profilePictureUrl;
    GroupRole role;
    ZonedDateTime joinedAt;
    boolean isOnline;
}