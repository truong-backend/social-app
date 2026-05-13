package com.stu.socialnetworkapi.controller;

import com.stu.socialnetworkapi.dto.response.ApiResponse;
import com.stu.socialnetworkapi.dto.response.StoryGroupResponse;
import com.stu.socialnetworkapi.dto.response.StoryResponse;
import com.stu.socialnetworkapi.dto.response.StoryViewerResponse;
import com.stu.socialnetworkapi.service.itf.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/stories")
public class StoryController {

    private final StoryService storyService;

    /** GET /v1/stories/friends — story của bạn bè (group theo user) */
    @GetMapping("/friends")
    public ApiResponse<List<StoryGroupResponse>> getFriendStories() {
        return ApiResponse.success(storyService.getFriendStories());
    }

    /** GET /v1/stories/mine — story của chính mình */
    @GetMapping("/mine")
    public ApiResponse<List<StoryResponse>> getMyStories() {
        return ApiResponse.success(storyService.getMyStories());
    }

    /** POST /v1/stories — tạo story mới (multipart) */
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<StoryResponse> createStory(
            @RequestPart(value = "media", required = false) MultipartFile media,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "bgColor", required = false) String bgColor
    ) {
        return ApiResponse.success(storyService.createStory(media, caption, bgColor));
    }

    /** POST /v1/stories/{id}/view — đánh dấu đã xem */
    @PostMapping("/{id}/view")
    public ApiResponse<Void> viewStory(@PathVariable UUID id) {
        storyService.viewStory(id);
        return ApiResponse.success();
    }

    /** DELETE /v1/stories/{id} — xoá story */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStory(@PathVariable UUID id) {
        storyService.deleteStory(id);
        return ApiResponse.success();
    }

    /** GET /v1/stories/{id}/viewers — danh sách người đã xem (owner only) */
    @GetMapping("/{id}/viewers")
    public ApiResponse<List<StoryViewerResponse>> getViewers(@PathVariable UUID id) {
        return ApiResponse.success(storyService.getViewers(id));
    }
}