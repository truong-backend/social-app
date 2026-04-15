package com.socialapp.infrastructure.adapter.file;

import com.socialapp.application.port.FileStoragePort;
import com.socialapp.domain.model.valueobject.FileMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Lưu file vào local disk.
 * Có thể swap sang S3/MinIO bằng cách implement lại interface này.
 *
 * Validate nghiệp vụ:
 *   - Kích thước <= 10MB
 *   - Không cho phép .exe, .bat, .sh, .js
 */
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final List<String> BLOCKED_EXTENSIONS =
            List.of(".exe", ".bat", ".sh", ".js");

    private final Path uploadRoot;

    public LocalFileStorageAdapter(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + uploadRoot, e);
        }
    }

    @Override
    public FileMeta store(MultipartFile file) {
        // 1. Validate size
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new IllegalArgumentException(
                    "File '" + file.getOriginalFilename() + "' vượt quá giới hạn 10MB");

        // 2. Validate extension
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "unknown";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase()
                : "";
        if (BLOCKED_EXTENSIONS.contains(ext))
            throw new IllegalArgumentException(
                    "Loại tập tin '" + ext + "' không được phép tải lên");

        // 3. Generate unique filename để tránh trùng
        String storedName = UUID.randomUUID() + ext;
        Path   targetPath = uploadRoot.resolve(storedName);

        // 4. Write to disk
        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + originalName, e);
        }

        // 5. Trả về FileMeta — FileMeta VO sẽ validate lại lần nữa (defense in depth)
        String relativePath = storedName; // hoặc URL đầy đủ nếu dùng S3
        return new FileMeta(
                relativePath,
                originalName,
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public void delete(String path) {
        try {
            Path target = uploadRoot.resolve(path);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa file: " + path, e);
        }
    }
}