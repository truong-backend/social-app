package com.stu.socialnetworkapi.service.impl;

import com.stu.socialnetworkapi.config.MinioProperties;
import com.stu.socialnetworkapi.exception.ApiException;
import com.stu.socialnetworkapi.exception.ErrorCode;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build()
            );
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build()
                );
                log.info("MinIO bucket '{}' created successfully.", minioProperties.getBucket());
            } else {
                log.info("MinIO bucket '{}' already exists.", minioProperties.getBucket());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
            throw new ApiException(ErrorCode.STORAGE_INITIALIZATION_ERROR);
        }
    }

    /**
     * Upload file to MinIO asynchronously.
     */
    @Async
    public void uploadAsync(MultipartFile file, String objectName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.debug("Uploaded file '{}' to MinIO.", objectName);
        } catch (Exception e) {
            log.error("Failed to upload file '{}' to MinIO", objectName, e);
            throw new ApiException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    /**
     * Get an InputStream for a stored object.
     */
    public InputStream getObject(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get file '{}' from MinIO", objectName, e);
            throw new ApiException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    /**
     * Generate a presigned GET URL valid for the configured duration.
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry((int) minioProperties.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for '{}'", objectName, e);
            throw new ApiException(ErrorCode.LOAD_FILE_FAILED);
        }
    }

    /**
     * Delete an object from MinIO.
     */
    public void deleteObject(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .build()
            );
            log.debug("Deleted file '{}' from MinIO.", objectName);
        } catch (Exception e) {
            log.error("Failed to delete file '{}' from MinIO", objectName, e);
            throw new ApiException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    /**
     * Check whether an object exists in MinIO.
     */
    public boolean objectExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}