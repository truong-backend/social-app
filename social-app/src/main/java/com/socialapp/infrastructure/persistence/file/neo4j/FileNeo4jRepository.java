package com.socialapp.infrastructure.persistence.file.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;

public interface FileNeo4jRepository extends Neo4jRepository<FileNodeNeo4j, String> {

    List<FileNodeNeo4j> findByPathIn(List<String> paths);

    void deleteByPath(String path);

    void deleteByPathIn(List<String> paths);
}
