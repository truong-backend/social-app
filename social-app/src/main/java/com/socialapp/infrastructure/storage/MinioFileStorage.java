package com.socialapp.infrastructure.storage;

import com.socialapp.application.shared.port.FileStorage;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MinioFileStorage implements FileStorage {

    private final MinioClient client;
    private final String      bucket;

    public MinioFileStorage(
            @Value("${app.minio.url}")        String url,
            @Value("${app.minio.access-key}") String accessKey,
            @Value("${app.minio.secret-key}") String secretKey,
            @Value("${app.minio.bucket}")     String bucket) {
        this.client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            String path = UUID.randomUUID() + "_" + file.getOriginalFilename();
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return path;
        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket).object(path).build());
        } catch (Exception e) {
            log.warn("Failed to delete file {}: {}", path, e.getMessage());
        }
    }

    @Override
    public void deleteAll(List<String> paths) {
        paths.forEach(this::delete);
    }
}