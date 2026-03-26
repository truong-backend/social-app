package com.socialapp.domain.file.repository;

import com.socialapp.domain.file.entity.FileNode;

import java.util.List;
import java.util.Optional;

public interface FileRepository {

    Optional<FileNode> findByPath(String path);

    List<FileNode> findByPaths(List<String> paths);

    FileNode save(FileNode fileNode);

    void deleteByPath(String path);

    void deleteByPaths(List<String> paths);
}