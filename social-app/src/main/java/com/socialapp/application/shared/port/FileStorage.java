package com.socialapp.application.shared.port;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Outbound Port: FileStorage
 * Được implement ở infrastructure (S3 / MinIO / local disk)
 */
public interface FileStorage {
    String upload(MultipartFile file);
    void delete(String path);
    void deleteAll(List<String> paths);
}