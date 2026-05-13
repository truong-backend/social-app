package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.StoryGroupResponse;
import com.stu.socialnetworkapi.dto.response.StoryResponse;
import com.stu.socialnetworkapi.dto.response.StoryViewerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface StoryService {

    /** Danh sách story của bạn bè, group theo user */
    List<StoryGroupResponse> getFriendStories();

    /** Story của chính mình */
    List<StoryResponse> getMyStories();

    /** Tạo story mới: upload ảnh/video hoặc text+bgColor */
    StoryResponse createStory(MultipartFile media, String caption, String bgColor);

    /** Đánh dấu đã xem story */
    void viewStory(UUID storyId);

    /** Xoá story (chỉ owner) */
    void deleteStory(UUID storyId);

    /** Danh sách người đã xem (chỉ owner) */
    List<StoryViewerResponse> getViewers(UUID storyId);
}