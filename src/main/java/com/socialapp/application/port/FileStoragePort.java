package com.socialapp.application.port;

import com.socialapp.domain.model.valueobject.FileMeta;
import org.springframework.web.multipart.MultipartFile;

/**
 * Port (outbound) — Infrastructure sẽ implement (local disk / S3 / MinIO).
 */
public interface FileStoragePort {
    /**
     * Upload file lên storage, trả về FileMeta chứa path truy cập.
     * Validate size <= 10MB và type không phải executable ở đây.
     */
    FileMeta store(MultipartFile file);

    void delete(String path);
}