package com.stu.socialnetworkapi.repository.neo4j;

import com.stu.socialnetworkapi.entity.Story;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoryRepository extends Neo4jRepository<Story, UUID> {

    Optional<Story> findByIdAndDeletedAtIsNull(UUID id);

    @Query("""
        MATCH (viewer:User {id: $viewerId})
        MATCH (story:Story {id: $storyId})
        MERGE (viewer)-[v:VIEWED_STORY]->(story)
        ON CREATE SET v.viewedAt = $viewedAt,
                      story.viewCount = story.viewCount + 1
        """)
    void markViewed(UUID storyId, UUID viewerId, ZonedDateTime viewedAt);
}