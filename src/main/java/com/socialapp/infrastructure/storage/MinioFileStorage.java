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
    private final String      minioPublicUrl;

    public MinioFileStorage(
            @Value("${app.minio.url}")        String url,
            @Value("${app.minio.access-key}") String accessKey,
            @Value("${app.minio.secret-key}") String secretKey,
            @Value("${app.minio.bucket}")     String bucket,
            @Value("${app.minio.public-url:#{null}}") String publicUrl) {
        this.client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        // Nếu không cấu hình riêng public-url thì dùng luôn url của MinIO
        this.minioPublicUrl = (publicUrl != null && !publicUrl.isBlank()) ? publicUrl : url;
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

    /**
     * Trả về URL công khai để browser có thể load ảnh/file trực tiếp.
     * Bucket cần được set policy public-read trong MinIO.
     * Ví dụ: http://localhost:9000/socialapp/uuid_avatar.jpg
     */
    @Override
    public String getPublicUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return minioPublicUrl + "/" + bucket + "/" + path;
    }
}
