package com.socialapp.domain.model.valueobject;

import java.util.List;
import java.util.Objects;

/**
 * Metadata của file upload.
 * Rules:
 *   - Kích thước ≤ 10MB
 *   - Không được phép upload .exe, .bat, .sh, .js
 */
public final class FileMeta {

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final List<String> BLOCKED_EXTENSIONS =
            List.of(".exe", ".bat", ".sh", ".js");

    private final String path;
    private final String name;
    private final String contentType;
    private final long   sizeBytes;

    public FileMeta(String path, String name, String contentType, long sizeBytes) {
        if (path == null || path.isBlank())
            throw new IllegalArgumentException("File path cannot be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("File name cannot be blank");
        if (sizeBytes > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("File exceeds 10MB limit");

        String ext = name.contains(".")
                ? name.substring(name.lastIndexOf('.')).toLowerCase()
                : "";
        if (BLOCKED_EXTENSIONS.contains(ext))
            throw new IllegalArgumentException("File type '" + ext + "' is not allowed");

        this.path        = path;
        this.name        = name;
        this.contentType = contentType;
        this.sizeBytes   = sizeBytes;
    }

    public String getPath()        { return path; }
    public String getName()        { return name; }
    public String getContentType() { return contentType; }
    public long   getSizeBytes()   { return sizeBytes; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof FileMeta)) return false;
        return path.equals(((FileMeta) o).path);
    }

    @Override public int hashCode() { return Objects.hash(path); }
}