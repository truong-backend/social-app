package com.socialapp.domain.file.entity;

import com.socialapp.domain.file.exception.FileDomainException;

/**
 * Entity: FileNode
 * Đại diện cho một file đã được upload lên hệ thống.
 * Key là path (unique trên storage).
 */
public class FileNode {

    private final String path;
    private final String name;
    private final ContentType contentType;

    private FileNode(String path, String name, ContentType contentType) {
        if (path == null || path.isBlank())
            throw new FileDomainException("File path must not be blank");
        if (name == null || name.isBlank())
            throw new FileDomainException("File name must not be blank");
        this.path        = path;
        this.name        = name;
        this.contentType = contentType;
    }

    public static FileNode create(String path, String originalName, String contentTypeStr) {
        return new FileNode(path, originalName, ContentType.of(contentTypeStr));
    }

    public static FileNode reconstitute(String path, String name, String contentTypeStr) {
        return new FileNode(path, name, ContentType.of(contentTypeStr));
    }

    public String getPath()             { return path; }
    public String getName()             { return name; }
    public ContentType getContentType() { return contentType; }
}