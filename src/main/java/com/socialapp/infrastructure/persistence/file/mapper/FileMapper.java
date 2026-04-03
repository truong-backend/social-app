package com.socialapp.infrastructure.persistence.file.mapper;


import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.infrastructure.persistence.file.neo4j.FileNodeNeo4j;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileNode toDomain(FileNodeNeo4j n) {
        return FileNode.reconstitute(n.getPath(), n.getName(), n.getContentType());
    }

    public FileNodeNeo4j toNode(FileNode f) {
        return FileNodeNeo4j.builder()
                .path(f.getPath())
                .name(f.getName())
                .contentType(f.getContentType().getValue())
                .build();
    }
}