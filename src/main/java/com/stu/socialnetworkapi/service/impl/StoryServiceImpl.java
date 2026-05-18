package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.dto.response.StoryGroupResponse;
import com.stu.socialnetworkapi.dto.response.StoryResponse;
import com.stu.socialnetworkapi.dto.response.StoryViewerResponse;
import com.stu.socialnetworkapi.entity.File;
import com.stu.socialnetworkapi.entity.Story;
import com.stu.socialnetworkapi.entity.User;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import com.stu.socialnetworkapi.repository.neo4j.StoryRepository;
import com.stu.socialnetworkapi.service.itf.FileService;
import com.stu.socialnetworkapi.service.itf.StoryService;
import com.stu.socialnetworkapi.service.itf.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final UserService userService;
    private final FileService fileService;
    private final Neo4jClient neo4jClient;

    private static final long STORY_TTL_HOURS = 24;

    @Override
    public List<StoryGroupResponse> getFriendStories() {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
//        ZonedDateTime since = ZonedDateTime.now().minusHours(STORY_TTL_HOURS);

        List<Map<String, Object>> rows = neo4jClient.query("""
                MATCH (me:User {id: $currentUserId})
                MATCH (author:User)-[:FRIEND]-(me)
                MATCH (author)-[:POSTED_STORY]->(story:Story)
                WHERE story.deletedAt IS NULL
//                  AND story.createdAt >= $since
                OPTIONAL MATCH (author)-[:HAS_PROFILE_PICTURE]->(pic:File)
                OPTIONAL MATCH (story)-[:STORY_MEDIA]->(media:File)
                OPTIONAL MATCH (me)-[viewed:VIEWED_STORY]->(story)
                WITH
                    author.id           AS authorId,
                    author.username     AS username,
                    author.givenName    AS givenName,
                    author.familyName   AS familyName,
                    pic.id              AS profilePicId,
                    story.id            AS storyId,
                    story.caption       AS caption,
                    story.bgColor       AS bgColor,
                    story.mediaType     AS mediaType,
                    story.viewCount     AS viewCount,
                    story.createdAt     AS createdAt,
                    media.id            AS mediaId,
                    viewed IS NOT NULL  AS isViewed
                RETURN DISTINCT authorId, username, givenName, familyName, profilePicId,
                    storyId, caption, bgColor, mediaType, viewCount, createdAt, mediaId, isViewed
                ORDER BY authorId, createdAt ASC
                """)
                .bind(currentUserId.toString()).to("currentUserId")
//                .bind(since).to("since")
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());

        // Deduplicate by storyId before grouping
        Map<String, Map<String, Object>> deduped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String storyId = row.get("storyId").toString();
            deduped.putIfAbsent(storyId, row);
        }

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : deduped.values()) {
            String authorId = row.get("authorId").toString();
            grouped.computeIfAbsent(authorId, k -> new ArrayList<>()).add(row);
        }

        return grouped.values().stream().map(group -> {
            Map<String, Object> first = group.get(0);
            String givenName  = (String) first.get("givenName");
            String familyName = (String) first.get("familyName");
            String username   = (String) first.get("username");
            String displayName = buildDisplayName(givenName, familyName, username);

            Object picId = first.get("profilePicId");
            String avatar = picId != null ? File.getPath(picId.toString()) : null;

            List<StoryResponse> stories = group.stream().map(r -> {
                Object mediaId   = r.get("mediaId");
                Object viewCount = r.get("viewCount");
                Object isViewed  = r.get("isViewed");
                Object createdAt = r.get("createdAt");
                return StoryResponse.builder()
                        .id(UUID.fromString(r.get("storyId").toString()))
                        .mediaUrl(mediaId != null ? File.getPath(mediaId.toString()) : null)
                        .mediaType((String) r.get("mediaType"))
                        .caption((String) r.get("caption"))
                        .bgColor((String) r.get("bgColor"))
                        .createdAt(createdAt != null ? ZonedDateTime.parse(createdAt.toString()) : null)
                        .viewCount(viewCount != null ? ((Number) viewCount).intValue() : 0)
                        .isViewed(isViewed != null && (Boolean) isViewed)
                        .build();
            }).collect(Collectors.toList());

            boolean hasNewStory = stories.stream().anyMatch(s -> !s.isViewed());

            return StoryGroupResponse.builder()
                    .userId(UUID.fromString(first.get("authorId").toString()))
                    .username(username)
                    .displayName(displayName)
                    .avatar(avatar)
                    .hasNewStory(hasNewStory)
                    .stories(stories)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<StoryResponse> getMyStories() {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
//        ZonedDateTime since = ZonedDateTime.now().minusHours(STORY_TTL_HOURS);

        List<Map<String, Object>> rows = neo4jClient.query("""
                MATCH (me:User {id: $currentUserId})-[:POSTED_STORY]->(story:Story)
                WHERE story.deletedAt IS NULL
//                  AND story.createdAt >= $since
                OPTIONAL MATCH (story)-[:STORY_MEDIA]->(media:File)
                WITH
                    story.id        AS storyId,
                    story.caption   AS caption,
                    story.bgColor   AS bgColor,
                    story.mediaType AS mediaType,
                    story.viewCount AS viewCount,
                    story.createdAt AS createdAt,
                    media.id        AS mediaId
                RETURN DISTINCT storyId, caption, bgColor, mediaType, viewCount, createdAt, mediaId
                ORDER BY createdAt ASC
                """)
                .bind(currentUserId.toString()).to("currentUserId")
//                .bind(since).to("since")
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());

        // Deduplicate by storyId
        Map<String, Map<String, Object>> deduped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String storyId = row.get("storyId").toString();
            deduped.putIfAbsent(storyId, row);
        }

        return deduped.values().stream()
                .map(r -> {
                    Object mediaId   = r.get("mediaId");
                    Object viewCount = r.get("viewCount");
                    Object createdAt = r.get("createdAt");
                    return StoryResponse.builder()
                            .id(UUID.fromString(r.get("storyId").toString()))
                            .mediaUrl(mediaId != null ? File.getPath(mediaId.toString()) : null)
                            .mediaType((String) r.get("mediaType"))
                            .caption((String) r.get("caption"))
                            .bgColor((String) r.get("bgColor"))
                            .createdAt(createdAt != null ? ZonedDateTime.parse(createdAt.toString()) : null)
                            .viewCount(viewCount != null ? ((Number) viewCount).intValue() : 0)
                            .isViewed(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public StoryResponse createStory(MultipartFile media, String caption, String bgColor) {
        User author = userService.getCurrentUserRequiredAuthentication();

        File uploadedFile = null;
        String mediaType = "text";

        if (media != null && !media.isEmpty()) {
            uploadedFile = fileService.upload(media);
            String ct = media.getContentType() != null ? media.getContentType() : "";
            mediaType = ct.startsWith("video") ? "video" : "image";
        }

        Story story = Story.builder()
                .author(author)
                .caption(caption)
                .bgColor(bgColor)
                .mediaType(mediaType)
                .mediaFile(uploadedFile)
                .viewCount(0)
                .build();

        Story saved = storyRepository.save(story);

        return StoryResponse.builder()
                .id(saved.getId())
                .mediaUrl(uploadedFile != null ? File.getPath(uploadedFile) : null)
                .mediaType(saved.getMediaType())
                .caption(saved.getCaption())
                .bgColor(saved.getBgColor())
                .createdAt(saved.getCreatedAt())
                .viewCount(0)
                .isViewed(false)
                .build();
    }

    @Override
    public void viewStory(UUID storyId) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        storyRepository.findByIdAndDeletedAtIsNull(storyId)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_RESOURCE_FOUND));
        storyRepository.markViewed(storyId, currentUserId, ZonedDateTime.now());
    }

    @Override
    public void deleteStory(UUID storyId) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();
        Story story = storyRepository.findByIdAndDeletedAtIsNull(storyId)
                .orElseThrow(() -> new ApiException(ErrorCode.NO_RESOURCE_FOUND));
        if (!story.getAuthor().getId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        story.setDeletedAt(ZonedDateTime.now());
        storyRepository.save(story);
    }

    @Override
    public List<StoryViewerResponse> getViewers(UUID storyId) {
        UUID currentUserId = userService.getCurrentUserIdRequiredAuthentication();

        return neo4jClient.query("""
                MATCH (story:Story {id: $storyId})<-[:POSTED_STORY]-(author:User {id: $currentUserId})
                MATCH (viewer:User)-[v:VIEWED_STORY]->(story)
                OPTIONAL MATCH (viewer)-[:HAS_PROFILE_PICTURE]->(pic:File)
                RETURN
                    viewer.id           AS userId,
                    viewer.username     AS username,
                    viewer.givenName    AS givenName,
                    viewer.familyName   AS familyName,
                    pic.id              AS profilePicId,
                    v.viewedAt          AS viewedAt
                ORDER BY v.viewedAt DESC
                """)
                .bind(storyId.toString()).to("storyId")
                .bind(currentUserId.toString()).to("currentUserId")
                .fetch()
                .all()
                .stream()
                .map(r -> {
                    Object picId    = r.get("profilePicId");
                    Object viewedAt = r.get("viewedAt");
                    String givenName  = (String) r.get("givenName");
                    String familyName = (String) r.get("familyName");
                    String username   = (String) r.get("username");
                    return StoryViewerResponse.builder()
                            .userId(UUID.fromString(r.get("userId").toString()))
                            .username(username)
                            .displayName(buildDisplayName(givenName, familyName, username))
                            .avatar(picId != null ? File.getPath(picId.toString()) : null)
                            .viewedAt(viewedAt != null ? ZonedDateTime.parse(viewedAt.toString()) : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String buildDisplayName(String givenName, String familyName, String username) {
        String name = ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
        return name.isEmpty() ? username : name;
    }
}