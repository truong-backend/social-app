package com.socialapp.domain.file.entity;

import com.socialapp.domain.file.exception.FileDomainException;

import java.util.Objects;
import java.util.Set;

/**
 * Value Object: ContentType
 * Enforce danh sách MIME type được phép upload.
 */
public final class ContentType {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm",
            "audio/mpeg", "audio/ogg",
            "application/pdf",
            "application/octet-stream"
    );

    private final String value;

    private ContentType(String value) {
        if (value == null || value.isBlank())
            throw new FileDomainException("ContentType must not be blank");
        if (!ALLOWED_TYPES.contains(value.toLowerCase()))
            throw new FileDomainException("Unsupported content type: " + value);
        this.value = value.toLowerCase();
    }

    public static ContentType of(String value) {
        return new ContentType(value);
    }

    public boolean isImage() {
        return value.startsWith("image/");
    }

    public boolean isVideo() {
        return value.startsWith("video/");
    }

    public boolean isAudio() {
        return value.startsWith("audio/");
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentType ct)) return false;
        return Objects.equals(value, ct.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}