package com.socialapp.domain.model.entity;

import com.socialapp.domain.model.valueobject.FileMeta;

/**
 * Entity: File
 * Identity: path (unique trong hệ thống)
 */
public class FileEntity {

    private final FileMeta meta;

    public FileEntity(FileMeta meta) {
        if (meta == null) throw new IllegalArgumentException("FileMeta is required");
        this.meta = meta;
    }

    public FileMeta getMeta() { return meta; }

    // identity dựa trên path
    @Override public boolean equals(Object o) {
        if (!(o instanceof FileEntity)) return false;
        return meta.getPath().equals(((FileEntity) o).meta.getPath());
    }

    @Override public int hashCode() { return meta.getPath().hashCode(); }
}