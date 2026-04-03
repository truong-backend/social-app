package com.socialapp.infrastructure.persistence.file.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileNeo4jRepository extends Neo4jRepository<FileNodeNeo4j, String> {

    List<FileNodeNeo4j> findByPathIn(List<String> paths);

    void deleteByPath(String path);

    void deleteByPathIn(List<String> paths);

    // (User)-[:UPLOAD_FILE]->(File)
    @Query("""
           MATCH (u:User {id: $userId}), (f:File {path: $filePath})
           MERGE (u)-[:UPLOAD_FILE]->(f)
           """)
    void linkUploadFile(String userId, String filePath);
}