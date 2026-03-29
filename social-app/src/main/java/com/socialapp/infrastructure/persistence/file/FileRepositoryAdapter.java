package com.socialapp.infrastructure.persistence.file;

import com.socialapp.domain.file.entity.FileNode;
import com.socialapp.domain.file.repository.FileRepository;
import com.socialapp.infrastructure.persistence.file.mapper.FileMapper;
import com.socialapp.infrastructure.persistence.file.neo4j.FileNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileRepositoryAdapter implements FileRepository {

    private final FileNeo4jRepository neo4j;
    private final FileMapper mapper;

    @Override
    public Optional<FileNode> findByPath(String path) {
        return neo4j.findById(path).map(mapper::toDomain);
    }

    @Override
    public List<FileNode> findByPaths(List<String> paths) {
        return neo4j.findByPathIn(paths)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public FileNode save(FileNode fileNode) {
        return mapper.toDomain(neo4j.save(mapper.toNode(fileNode)));
    }

    @Override
    public void deleteByPath(String path) {
        neo4j.deleteByPath(path);
    }

    @Override
    public void deleteByPaths(List<String> paths) {
        neo4j.deleteByPathIn(paths);
    }
}